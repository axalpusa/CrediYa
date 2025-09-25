package co.com.pragma.sqs.sender.config;

import co.com.pragma.model.order.CapacityResponse;
import co.com.pragma.model.order.Order;
import co.com.pragma.model.order.gateways.OrderRepository;
import co.com.pragma.sqs.sender.ResultQueuePoller;
import co.com.pragma.sqs.sender.config.SQSSenderProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import enums.StatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ResultQueuePollerTest {

    private SqsAsyncClient client;
    private ObjectMapper objectMapper;
    private SQSSenderProperties properties;
    private OrderRepository orderRepository;
    private ResultQueuePoller poller;

    @BeforeEach
    void setUp() {
        client = mock(SqsAsyncClient.class);
        objectMapper = new ObjectMapper();
        properties = mock(SQSSenderProperties.class);
        orderRepository = mock(OrderRepository.class);
        poller = new ResultQueuePoller(client, objectMapper, properties, orderRepository);

        when(properties.calculateCapacityResponseQueueUrl()).thenReturn("http://localhost/queue");
    }

    @Test
    void processMessage_updatesOrderAndDeletesMessage() throws Exception {
        // given
        UUID orderId = UUID.randomUUID();
        CapacityResponse dto = new CapacityResponse(orderId, "APPROVED");
        String body = objectMapper.writeValueAsString(dto);

        Message message = Message.builder()
                .messageId("123")
                .receiptHandle("abc")
                .body(body)
                .build();

        Order order = new Order();
        order.setIdOrder(orderId);

        when(orderRepository.findById(orderId)).thenReturn(Mono.just(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(client.deleteMessage(any(DeleteMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(DeleteMessageResponse.builder().build()));

        pollerTestHook_processMessage(poller, message);

        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(any(Order.class));
        verify(client).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void processMessage_handlesInvalidJsonWithoutDeleting() {
        // given
        Message badMessage = Message.builder()
                .messageId("456")
                .receiptHandle("def")
                .body("not-a-json")
                .build();

        pollerTestHook_processMessage(poller, badMessage);

        verify(client, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    private void pollerTestHook_processMessage(ResultQueuePoller poller, Message message) {
        try {
            var method = ResultQueuePoller.class.getDeclaredMethod("processMessage", Message.class);
            method.setAccessible(true);
            method.invoke(poller, message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
