package co.com.pragma.sqs.sender;

import co.com.pragma.model.order.LambdaResponse;
import co.com.pragma.model.order.Order;
import co.com.pragma.model.order.gateways.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import enums.StatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LambdaResponseListener {

    private final SqsAsyncClient sqsClient;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;
    private final String responseQueueUrl = "URL_COLA";

    public void startListening() {
        Mono.fromRunnable ( this::pollQueue )
                .repeat ( )
                .subscribe ( );
    }

    private void pollQueue() {
        sqsClient.receiveMessage ( r -> r.queueUrl ( responseQueueUrl ).maxNumberOfMessages ( 10 ).waitTimeSeconds ( 20 ) )
                .thenAccept ( response -> {
                    response.messages ( ).forEach ( msg -> {
                        try {
                            LambdaResponse lambdaResponse = objectMapper.readValue ( msg.body ( ), LambdaResponse.class );
                            updateOrderStatus ( lambdaResponse ).subscribe ( );
                            sqsClient.deleteMessage ( d -> d.queueUrl ( responseQueueUrl ).receiptHandle ( msg.receiptHandle ( ) ) );
                        } catch (Exception e) {
                            log.error("Error procesando mensaje SQS", e);
                        }
                    } );
                } );
    }

    private Mono < Order > updateOrderStatus(LambdaResponse lambdaResponse) {
        return orderRepository.findById ( lambdaResponse.getIdOrder ( ) )
                .flatMap ( order -> {
                    UUID newStatus  = StatusEnum.getIdFromName ( lambdaResponse.getStatus ( ) );
                    order.setIdStatus ( newStatus  );
                    return orderRepository.save ( order );
                } );
    }
}

