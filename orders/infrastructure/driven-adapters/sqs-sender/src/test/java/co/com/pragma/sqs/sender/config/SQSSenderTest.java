package co.com.pragma.sqs.sender.config;

import co.com.pragma.model.order.Order;
import co.com.pragma.sqs.sender.SQSSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import enums.StatusEnum;
import enums.TypeLoanEnum;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SQSSenderTest {

    @Test
    void sendOrderStatus_shouldSendMessage() {
        SqsAsyncClient client = mock(SqsAsyncClient.class);
        ObjectMapper mapper = new ObjectMapper();
        SQSSenderProperties props = new SQSSenderProperties("us-east-1", "https://sqs-url");

        SQSSender sender = new SQSSender(props, client, mapper);
        Order order = new Order();
        order.setIdOrder(UUID.randomUUID());
        order.setEmailAddress("axalpusa@gmail.com");
        order.setTermMonths(12);
        order.setAmount(new BigDecimal("1000"));
        order.setDocumentId("48295730");
        order.setIdTypeLoan(TypeLoanEnum.TYPE2.getId());
        order.setIdStatus(StatusEnum.PENDENT.getId());
        when(client.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(
                        SendMessageResponse.builder().messageId("123").build()
                ));

        StepVerifier.create(sender.sendOrderStatus(order))
                .verifyComplete();
    }
}
