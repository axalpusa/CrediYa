package co.com.pragma.sqs.sender;

import co.com.pragma.model.order.CapacityResponse;
import co.com.pragma.model.order.gateways.OrderRepository;
import co.com.pragma.sqs.sender.config.SQSSenderProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import enums.StatusEnum;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class ResultQueuePoller {

    private final SqsAsyncClient client;
    private final ObjectMapper objectMapper;
    private final SQSSenderProperties properties;
    private final OrderRepository orderRepository;

    @PostConstruct
    public void startPolling() {
        pollMessages ( );
    }

    private void pollMessages() {
        ReceiveMessageRequest request = ReceiveMessageRequest.builder ( )
                .queueUrl ( properties.calculateCapacityResponseQueueUrl ( ) )
                .maxNumberOfMessages ( 10 )
                .waitTimeSeconds ( 20 )
                .visibilityTimeout ( 30 )
                .build ( );

        Mono.fromFuture ( () -> client.receiveMessage ( request ) )
                .subscribeOn ( Schedulers.boundedElastic ( ) )
                .doOnError ( err -> log.error ( " Error recibiendo mensajes de SQS", err ) )
                .subscribe ( response -> {
                    List < Message > messages = response.messages ( );
                    if ( !messages.isEmpty ( ) ) {
                        log.info ( "Recibidos {} mensajes de respuesta_calculo", messages.size ( ) );
                        messages.forEach ( this::processMessage );
                    }
                    pollMessages ( );
                } );
    }

    private void processMessage(Message message) {
        try {
            log.info ( "Procesando mensaje: {}", message.body ( ) );
            CapacityResponse dto = objectMapper.readValue ( message.body ( ), CapacityResponse.class );
            log.info ( "Resultado cálculo recibido: orderId={}, decision={}", dto.getOrderId ( ), dto.getDecision ( ) );
            orderRepository.findById ( (dto.getOrderId ( )) )
                    .flatMap ( order -> {
                        UUID idStatus = StatusEnum.getIdFromName ( dto.getDecision ( ) );
                        order.setIdStatus ( idStatus );
                        return orderRepository.save ( order );
                    } )
                    .doOnSuccess ( saved -> {
                        log.info ( "Orden {} actualizada en la BD", saved.getIdOrder ( ) );
                        deleteMessage ( message );
                    } )
                    .doOnError ( err -> log.error ( "Error actualizando orden {}", dto.getOrderId ( ), err ) )
                    .subscribe ( );

        } catch (Exception e) {
            log.error ( "Error procesando mensaje", e );
        }
    }


    private void deleteMessage(Message message) {
        DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder ( )
                .queueUrl ( properties.calculateCapacityResponseQueueUrl ( ) )
                .receiptHandle ( message.receiptHandle ( ) )
                .build ( );

        client.deleteMessage ( deleteRequest ).whenComplete ( (resp, ex) -> {
            if ( ex != null ) {
                log.error ( "Error eliminando mensaje: {}", message.messageId ( ), ex );
            } else {
                log.info ( "Mensaje eliminado: {}", message.messageId ( ) );
            }
        } );
    }
}