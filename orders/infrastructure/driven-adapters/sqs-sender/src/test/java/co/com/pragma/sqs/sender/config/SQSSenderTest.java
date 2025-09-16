package co.com.pragma.sqs.sender.config;

import co.com.pragma.model.order.CalculateCapacity;
import co.com.pragma.model.order.Order;
import co.com.pragma.sqs.sender.SQSSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import enums.StatusEnum;
import enums.TypeLoanEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SQSSenderTest {

    private SQSSender sqsSender;
    private SqsAsyncClient sqsClient;
    private SQSSenderProperties properties;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        properties = new SQSSenderProperties ( "us-east-2",
                "http://localhost:4566/000000000000/calculate-queue",
                "http://localhost:4566/000000000000/status-queue"
        );

        sqsClient = mock ( SqsAsyncClient.class );
        objectMapper = new ObjectMapper ( );
        sqsSender = new SQSSender ( properties, sqsClient, objectMapper );
    }

    @Test
    void send_shouldReturnMessageId_whenSqsRespondsSuccessfully() {
        String expectedMessageId = "12345";
        SendMessageResponse response = SendMessageResponse.builder ( ).messageId ( expectedMessageId ).build ( );

        when ( sqsClient.sendMessage ( any ( SendMessageRequest.class ) ) )
                .thenReturn ( CompletableFuture.completedFuture ( response ) );

        StepVerifier.create ( sqsSender.sendStatus ( "test-message" ) )
                .expectNext ( expectedMessageId )
                .verifyComplete ( );

        ArgumentCaptor < SendMessageRequest > captor = ArgumentCaptor.forClass ( SendMessageRequest.class );
        verify ( sqsClient ).sendMessage ( captor.capture ( ) );

        SendMessageRequest request = captor.getValue ( );
        assertThat ( request.queueUrl ( ) ).isEqualTo ( properties.orderStatusQueueUrl ( ) );
        assertThat ( request.messageBody ( ) ).isEqualTo ( "test-message" );
    }

    @Test
    void sendOrderStatus_shouldComplete_whenValidOrder() {
        UUID orderId = UUID.randomUUID ( );
        Order order = Order.builder ( )
                .idOrder ( orderId )
                .amount ( BigDecimal.TEN )
                .emailAddress ( "test@mail.com" )
                .idStatus ( StatusEnum.APPROVED.getId ( ) )
                .build ( );

        when ( sqsClient.sendMessage ( any ( SendMessageRequest.class ) ) )
                .thenReturn ( CompletableFuture.completedFuture (
                        SendMessageResponse.builder ( ).messageId ( "ok123" ).build ( )
                ) );

        StepVerifier.create ( sqsSender.sendOrderStatus ( order ) )
                .verifyComplete ( );

        verify ( sqsClient, times ( 1 ) ).sendMessage ( any ( SendMessageRequest.class ) );
    }

    @Test
    void sendOrderStatus_shouldError_whenInvalidStatus() {
        Order order = Order.builder ( )
                .idOrder ( UUID.randomUUID ( ) )
                .emailAddress ( "test@mail.com" )
                .idStatus ( UUID.randomUUID ( ) )
                .build ( );

        StepVerifier.create ( sqsSender.sendOrderStatus ( order ) )
                .expectError ( IllegalArgumentException.class )
                .verify ( );

        verify ( sqsClient, never ( ) ).sendMessage ( any ( SendMessageRequest.class ) );
    }

    @Test
    void sendOrderStatus_shouldError_whenJsonFails() throws Exception {
        Order order = Order.builder ( )
                .idOrder ( UUID.randomUUID ( ) )
                .emailAddress ( "test@mail.com" )
                .idStatus ( StatusEnum.REJECTED.getId ( ) )
                .build ( );

        ObjectMapper mockMapper = mock ( ObjectMapper.class );
        when ( mockMapper.writeValueAsString ( any ( ) ) ).thenThrow ( new RuntimeException ( "JSON error" ) );

        SQSSender brokenSender = new SQSSender ( properties, sqsClient, mockMapper );

        StepVerifier.create ( brokenSender.sendOrderStatus ( order ) )
                .expectError ( RuntimeException.class )
                .verify ( );

        verify ( sqsClient, never ( ) ).sendMessage ( any ( SendMessageRequest.class ) );

    }

    @Test
    void sendOrderStatus_shouldSendMessage() {
        SqsAsyncClient client = mock ( SqsAsyncClient.class );
        ObjectMapper mapper = new ObjectMapper ( );
        SQSSenderProperties props = new SQSSenderProperties ( "us-east-2", "https://sqs-calculate-url", "https://sqs-status-url" );

        SQSSender sender = new SQSSender ( props, client, mapper );
        Order order = new Order ( );
        order.setIdOrder ( UUID.randomUUID ( ) );
        order.setEmailAddress ( "axalpusa@gmail.com" );
        order.setTermMonths ( 12 );
        order.setAmount ( new BigDecimal ( "1000" ) );
        order.setDocumentId ( "48295730" );
        order.setIdTypeLoan ( TypeLoanEnum.TYPE2.getId ( ) );
        order.setIdStatus ( StatusEnum.PENDENT.getId ( ) );
        when ( client.sendMessage ( any ( SendMessageRequest.class ) ) )
                .thenReturn ( CompletableFuture.completedFuture (
                        SendMessageResponse.builder ( ).messageId ( "123" ).build ( )
                ) );

        StepVerifier.create ( sender.sendOrderStatus ( order ) )
                .verifyComplete ( );
    }
    @Test
    void sendCapacity_shouldReturnMessageId_whenSqsRespondsSuccessfully() {
        String expectedMessageId = "abc123";
        SendMessageResponse response = SendMessageResponse.builder().messageId(expectedMessageId).build();
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(response));

        StepVerifier.create(sqsSender.sendCapacity("calculate-message"))
                .expectNext(expectedMessageId)
                .verifyComplete();

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());

        SendMessageRequest request = captor.getValue();
        assertThat(request.queueUrl()).isEqualTo(properties.calculateCapacityQueueUrl());
        assertThat(request.messageBody()).isEqualTo("calculate-message");
    }

    @Test
    void sendCalculateCapacity_shouldCompleteSuccessfully() throws Exception {
        CalculateCapacity dto = new CalculateCapacity();
        Order order = new Order();
        order.setIdOrder(UUID.randomUUID());
        order.setEmailAddress("test@mail.com");
        dto.setOrder(order);

        SendMessageResponse response = SendMessageResponse.builder().messageId("msg123").build();
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(response));

        StepVerifier.create(sqsSender.sendCalculateCapacity(dto))
                .verifyComplete();

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());
        assertThat(captor.getValue().queueUrl()).isEqualTo(properties.calculateCapacityQueueUrl());
    }

    @Test
    void sendCalculateCapacity_shouldError_whenJsonFails() throws Exception {
        ObjectMapper brokenMapper = mock(ObjectMapper.class);
        when(brokenMapper.writeValueAsString(any())).thenThrow(new RuntimeException("JSON error"));

        SQSSender brokenSender = new SQSSender(properties, sqsClient, brokenMapper);
        CalculateCapacity dto = new CalculateCapacity();
        dto.setOrder(new Order());

        StepVerifier.create(brokenSender.sendCalculateCapacity(dto))
                .expectError(RuntimeException.class)
                .verify();

        verify(sqsClient, never()).sendMessage(any(SendMessageRequest.class));
    }


}
