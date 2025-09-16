package co.com.pragma.sqs.sender;

import co.com.pragma.model.notification.gateways.NotificationRepository;
import co.com.pragma.model.order.CalculateCapacity;
import co.com.pragma.model.order.Order;
import co.com.pragma.sqs.sender.config.SQSSenderProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import enums.StatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.Map;

@Service
@Log4j2
@RequiredArgsConstructor
public class SQSSender implements NotificationRepository {
    private final SQSSenderProperties properties;
    private final SqsAsyncClient client;
    private final ObjectMapper objectMapper;

    public Mono < String > sendStatus(String message) {
        log.info ( "Intentando enviar mensaje a SQS: {}", message );
        return Mono.fromCallable ( () -> buildRequestForOrderStatus ( message ) )
                .flatMap ( request -> Mono.fromFuture ( client.sendMessage ( request ) ) )
                .doOnNext ( response -> log.info ( "Mensaje enviado con ID: {}", response.messageId ( ) ) )
                .doOnError ( e -> log.error ( "Error enviando mensaje SQS", e ) )
                .map ( SendMessageResponse::messageId );
    }


    private SendMessageRequest buildRequestForOrderStatus(String message) {
        return SendMessageRequest.builder ( )
                .queueUrl ( properties.orderStatusQueueUrl ( ) )
                .messageBody ( message )
                .build ( );
    }

    @Override
    public Mono < Void > sendOrderStatus(Order order) {
        String status;
        try {
            status = StatusEnum.fromId ( order.getIdStatus ( ) ).name ( );
        } catch (IllegalArgumentException e) {
            log.error ( "Estado de orden inválido: {}", order.getIdStatus ( ), e );
            return Mono.error ( e );
        }

        Map < String, Object > payload = Map.of (
                "idOrder", order.getIdOrder ( ),
                "status", status,
                "emailAddress", order.getEmailAddress ( )
        );

        String messageBody;
        try {
            messageBody = objectMapper.writeValueAsString ( payload );
        } catch (Exception e) {
            log.error ( "Error convirtiendo Order a JSON", e );
            return Mono.error ( e );
        }

        return sendStatus ( messageBody )
                .doOnNext ( messageId -> log.info ( "Mensaje enviado a SQS con ID: {}", messageId ) )
                .then ( );
    }

    public Mono < String > sendCapacity(String message) {
        log.info ( "Intentando enviar mensaje a SQS: {}", message );
        return Mono.fromCallable ( () -> buildRequestForCapacity ( message ) )
                .flatMap ( request -> Mono.fromFuture ( client.sendMessage ( request ) ) )
                .doOnNext ( response -> log.info ( "Mensaje enviado con ID: {}", response.messageId ( ) ) )
                .doOnError ( e -> log.error ( "Error enviando mensaje SQS", e ) )
                .map ( SendMessageResponse::messageId );
    }

    private SendMessageRequest buildRequestForCapacity(String message) {
        return SendMessageRequest.builder ( )
                .queueUrl ( properties.calculateCapacityQueueUrl ( ) )
                .messageBody ( message )
                .build ( );
    }

    @Override
    public Mono < Void > sendCalculateCapacity(CalculateCapacity calculateCapacity) {
        String messageBody;
        try {
            messageBody = objectMapper.writeValueAsString ( calculateCapacity );
        } catch (Exception e) {
            log.error ( "Error convirtiendo CalculateCapacity a JSON", e );
            return Mono.error ( e );
        }

        return sendCapacity ( messageBody )
                .doOnNext ( messageId -> log.info ( "Mensaje CalculateCapacity enviado a SQS con ID: {}", messageId ) )
                .then ( );
    }

}
