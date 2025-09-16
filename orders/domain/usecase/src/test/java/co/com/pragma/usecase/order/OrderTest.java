package co.com.pragma.usecase.order;

import co.com.pragma.model.notification.gateways.NotificationRepository;
import co.com.pragma.model.order.OrderPending;
import co.com.pragma.model.order.Order;
import co.com.pragma.model.order.gateways.OrderRepository;
import co.com.pragma.model.typeloan.TypeLoan;
import co.com.pragma.model.typeloan.gateways.TypeLoanRepository;
import constants.Constants;
import exceptions.ValidationPragmaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OrderTest {
    private OrderRepository orderRepository;
    private TypeLoanRepository typeLoanRepository;
    private OrderUseCase orderUseCase;
    private NotificationRepository notificationRepository;

    @BeforeEach
    void setUp() {
        orderRepository = Mockito.mock ( OrderRepository.class );
        typeLoanRepository = Mockito.mock ( TypeLoanRepository.class );
        notificationRepository = Mockito.mock ( NotificationRepository.class );
        orderUseCase = new OrderUseCase ( orderRepository, typeLoanRepository ,notificationRepository);
    }

    @Test
    void shouldReturnOrderWhenExists() {
        UUID idTypeLoan = UUID.fromString ( "a71e243b-e901-4e6e-b521-85ff39ac2f3e" );
        UUID idStatus = UUID.fromString ( "f8820448-a6ef-4d0d-beb8-130a71dc3fda" );
        UUID id = UUID.randomUUID ( );
        Order order = new Order ( );
        order.setAmount ( BigDecimal.valueOf ( 1000.00 ) );
        order.setDocumentId ( "48295730" );
        order.setTermMonths ( 12 );
        order.setEmailAddress ( "axalpusa@gmail.com" );
        order.setIdTypeLoan ( idTypeLoan );
        order.setIdStatus ( idStatus );

        when ( orderRepository.findById ( id ) ).thenReturn ( Mono.just ( order ) );

        StepVerifier.create ( orderUseCase.getOrderById ( id ) )
                .expectNextMatches ( u -> u.getEmailAddress ( ).equals ( "axalpusa@gmail.com" ) &&
                        u.getIdTypeLoan ( ).equals ( idTypeLoan ) &&
                        u.getIdStatus ( ).equals ( idStatus ) )
                .verifyComplete ( );

        verify ( orderRepository ).findById ( id );
    }


    @Test
    void shouldThrowExceptionWhenOrderNotFound() {
        UUID id = UUID.randomUUID ( );

        when ( orderRepository.findById ( id ) ).thenReturn ( Mono.empty ( ) );

        StepVerifier.create ( orderUseCase.getOrderById ( id ) )
                .expectErrorMatches ( throwable -> throwable instanceof ValidationPragmaException &&
                        throwable.getMessage ( ).contains ( id.toString ( ) ) )
                .verify ( );

        verify ( orderRepository ).findById ( id );
    }

    @Test
    void shouldDeleteOrderSuccessfully() {
        UUID id = UUID.randomUUID ( );

        when ( orderRepository.deleteById ( id ) ).thenReturn ( Mono.empty ( ) );

        StepVerifier.create ( orderUseCase.deleteOrderById ( id ) )
                .verifyComplete ( );

        verify ( orderRepository ).deleteById ( id );
    }

    @Test
    void shouldGetAllOrderSuccessfully() {
        Order order = new Order ( );
        UUID idTypeLoan = UUID.fromString ( "a71e243b-e901-4e6e-b521-85ff39ac2f3e" );
        UUID idStatus = UUID.fromString ( "f8820448-a6ef-4d0d-beb8-130a71dc3fda" );
        order.setAmount ( BigDecimal.valueOf ( 1000.00 ) );
        order.setDocumentId ( "48295730" );
        order.setTermMonths ( 12 );
        order.setEmailAddress ( "axalpusa@gmail.com" );
        order.setIdTypeLoan ( idTypeLoan );
        order.setIdStatus ( idStatus );

        when ( orderRepository.findAll ( ) ).thenReturn ( reactor.core.publisher.Flux.just ( order ) );

        StepVerifier.create ( orderUseCase.getAllOrders ( ) )
                .expectNextMatches ( u -> u.getEmailAddress ( ).equals ( "axalpusa@gmail.com" ) )
                .verifyComplete ( );

        verify ( orderRepository ).findAll ( );
    }

    @Test
    void merge_shouldUpdateNameAndDescription_whenOtherHasValues() {
        UUID id = UUID.randomUUID ( );
        UUID idTypeLoan = UUID.fromString ( "a71e243b-e901-4e6e-b521-85ff39ac2f3e" );
        UUID idStatus = UUID.fromString ( "f8820448-a6ef-4d0d-beb8-130a71dc3fda" );
        Order original = Order.builder ( )
                .idOrder ( id )
                .amount ( BigDecimal.valueOf ( 1000.00 ) )
                .documentId ( "48295730" )
                .termMonths ( 12 )
                .emailAddress ( "axalpusa@gmail.com" )
                .idTypeLoan ( idTypeLoan )
                .idStatus ( idStatus )
                .build ( );

        Order other = Order.builder ( )
                .emailAddress ( "axalpusa125@gmail.com" )
                .build ( );

        original.merge ( other );

        assertThat ( original.getAmount ( ) ).isEqualTo ( BigDecimal.valueOf ( 1000.00 ) );
        assertThat ( original.getDocumentId ( ) ).isEqualTo ( "48295730" );
        assertThat ( original.getEmailAddress ( ) ).isEqualTo ( "axalpusa125@gmail.com" );
        assertThat ( original.getIdStatus ( ) ).isEqualTo ( idStatus );
        assertThat ( original.getIdTypeLoan ( ) ).isEqualTo ( idTypeLoan );
        assertThat ( original.getTermMonths ( ) ).isEqualTo ( 12 );
    }

    @Test
    void saveOrder_shouldSaveSuccessfully_whenValid() {
        UUID idTypeLoan = UUID.randomUUID ( );
        Order order = Order.builder ( )
                .idTypeLoan ( idTypeLoan )
                .amount ( BigDecimal.valueOf ( 5000 ) )
                .documentId ( "12345678" )
                .emailAddress ( "test@example.com" )
                .termMonths ( 12 )
                .build ( );

        TypeLoan typeLoan = new TypeLoan ( );
        typeLoan.setMinimumAmount ( BigDecimal.valueOf ( 1000 ) );
        typeLoan.setMaximumAmount ( BigDecimal.valueOf ( 10000 ) );
        typeLoan.setAutomaticValidation ( false );

        when ( typeLoanRepository.findById ( idTypeLoan ) ).thenReturn ( Mono.just ( typeLoan ) );
        when ( orderRepository.save ( order ) ).thenReturn ( Mono.just ( order ) );

        StepVerifier.create ( orderUseCase.saveOrder ( order ) )
                .expectNextMatches ( saved -> saved.getAmount ( ).equals ( order.getAmount ( ) ) &&
                        saved.getIdTypeLoan ( ).equals ( order.getIdTypeLoan ( ) ) )
                .verifyComplete ( );

        verify ( orderRepository ).save ( order );
    }

    @Test
    void saveOrder_shouldFail_whenTypeLoanNotFound() {
        UUID idTypeLoan = UUID.randomUUID ( );
        Order order = Order.builder ( )
                .idTypeLoan ( idTypeLoan )
                .amount ( BigDecimal.valueOf ( 5000 ) )
                .documentId ( "12345678" )
                .emailAddress ( "test@example.com" )
                .termMonths ( 12 )
                .build ( );


        when ( typeLoanRepository.findById ( idTypeLoan ) ).thenReturn ( Mono.empty ( ) );

        StepVerifier.create ( orderUseCase.saveOrder ( order ) )
                .expectErrorMatches ( throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage ( ).equals (  Constants.REQUEST_EMPTY ) )
                .verify ( );
    }

    @Test
    void saveOrder_shouldFail_whenAmountOutOfRange() {
        UUID typeLoanId = UUID.randomUUID ( );
        Order order = Order.builder ( )
                .idTypeLoan ( typeLoanId )
                .amount ( BigDecimal.valueOf ( 15000 ) )
                .documentId ( "12345678" )
                .emailAddress ( "test@example.com" )
                .termMonths ( 12 )
                .build ( );

        co.com.pragma.model.typeloan.TypeLoan typeLoan = new co.com.pragma.model.typeloan.TypeLoan ( );
        typeLoan.setMinimumAmount ( BigDecimal.valueOf ( 1000 ) );
        typeLoan.setMaximumAmount ( BigDecimal.valueOf ( 10000 ) );

        when ( typeLoanRepository.findById ( typeLoanId ) ).thenReturn ( Mono.just ( typeLoan ) );

        StepVerifier.create ( orderUseCase.saveOrder ( order ) )
                .expectErrorMatches ( throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage ( ).equals ( "The amount is not within the allowed range" ) )
                .verify ( );
    }

    @Test
    void updateOrder_shouldSaveOrder() {
        Order order = Order.builder ( ).amount ( BigDecimal.valueOf ( 2000 ) ).build ( );
        when ( orderRepository.save ( order ) ).thenReturn ( Mono.just ( order ) );

        StepVerifier.create ( orderUseCase.updateOrder ( order ) )
                .expectNext ( order )
                .verifyComplete ( );

        verify ( orderRepository ).save ( order );
    }

    @Test
    void getOrderById_shouldReturnOrder_whenExists() {
        UUID id = UUID.randomUUID ( );
        Order order = Order.builder ( ).amount ( BigDecimal.valueOf ( 1000 ) ).build ( );
        when ( orderRepository.findById ( id ) ).thenReturn ( Mono.just ( order ) );

        StepVerifier.create ( orderUseCase.getOrderById ( id ) )
                .expectNext ( order )
                .verifyComplete ( );

        verify ( orderRepository ).findById ( id );
    }

    @Test
    void getOrderById_shouldFail_whenNotFound() {
        UUID id = UUID.randomUUID ( );
        when ( orderRepository.findById ( id ) ).thenReturn ( Mono.empty ( ) );

        StepVerifier.create ( orderUseCase.getOrderById ( id ) )
                .expectErrorMatches ( throwable ->
                        throwable instanceof ValidationPragmaException &&
                                ((ValidationPragmaException) throwable).getErrors ( ).get ( 0 ).contains ( id.toString ( ) ) )
                .verify ( );
    }

    @Test
    void deleteOrderById_shouldCompleteSuccessfully() {
        UUID id = UUID.randomUUID ( );
        when ( orderRepository.deleteById ( id ) ).thenReturn ( Mono.empty ( ) );

        StepVerifier.create ( orderUseCase.deleteOrderById ( id ) )
                .verifyComplete ( );

        verify ( orderRepository ).deleteById ( id );
    }

    @Test
    void getAllOrders_shouldReturnFluxOfOrders() {
        Order order = Order.builder ( ).amount ( BigDecimal.valueOf ( 1000 ) ).build ( );
        when ( orderRepository.findAll ( ) ).thenReturn ( Flux.just ( order ) );

        StepVerifier.create ( orderUseCase.getAllOrders ( ) )
                .expectNext ( order )
                .verifyComplete ( );

        verify ( orderRepository ).findAll ( );
    }

    @Test
    void findPendingOrders_shouldReturnPendingOrders() {
        UUID filterStatus = UUID.randomUUID ( );
        String filterEmail = "axalpusa1125@gmail.com";
        int page = 0;
        int size = 10;

        OrderPending pendingDTO = new OrderPending( );
        when ( orderRepository.findPendingOrders ( filterStatus, filterEmail, page, size ) )
                .thenReturn ( Flux.just ( pendingDTO ) );

        StepVerifier.create ( orderUseCase.findPendingOrders ( filterStatus, filterEmail, page, size ) )
                .expectNext ( pendingDTO )
                .verifyComplete ( );

        verify ( orderRepository ).findPendingOrders ( filterStatus,filterEmail, page, size );
    }

    @Test
    void saveOrder_shouldFail_whenValidationFails() {
        UUID typeLoanId = UUID.randomUUID();
        Order invalidOrder = Order.builder()
                .idTypeLoan(typeLoanId)
                .documentId("")
                .emailAddress("")
                .termMonths(-1)
                .amount(BigDecimal.valueOf(-500))
                .build();

        when(typeLoanRepository.findById(typeLoanId)).thenReturn(Mono.empty());

        StepVerifier.create(orderUseCase.saveOrder(invalidOrder))
                .expectErrorMatches(throwable ->
                        throwable instanceof ValidationPragmaException &&
                                ((ValidationPragmaException) throwable).getErrors().containsAll(
                                        List.of(
                                                "Document id is required.",
                                                "Email address is required.",
                                                "Term months is required.",
                                                "Amount is required"
                                        )
                                )
                )
                .verify();

        verify(typeLoanRepository).findById(typeLoanId);
    }

    @Test
    void updateOrder_shouldFail_whenOrderIsNull() {
        StepVerifier.create(orderUseCase.updateOrder(null))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().equals(Constants.ORDER_NOT_FOUND)
                )
                .verify();
    }

    @Test
    void updateOrderAndNotify_shouldSaveAndSendNotification() {
        Order order = Order.builder()
                .idOrder(UUID.randomUUID())
                .amount(BigDecimal.valueOf(3000))
                .emailAddress("notify@test.com")
                .build();

        when(orderRepository.save(order)).thenReturn(Mono.just(order));
        when(notificationRepository.sendOrderStatus(order)).thenReturn(Mono.empty());

        StepVerifier.create(orderUseCase.updateOrderAndNotify(order))
                .expectNext(order)
                .verifyComplete();

        verify(orderRepository).save(order);
        verify(notificationRepository).sendOrderStatus(order);
    }

    @Test
    void saveOrder_shouldSendToCapacityLambda_whenAutomaticValidationTrue() {
        UUID typeLoanId = UUID.randomUUID();
        Order order = Order.builder()
                .idTypeLoan(typeLoanId)
                .amount(BigDecimal.valueOf(5000))
                .documentId("12345678")
                .emailAddress("test@example.com")
                .termMonths(12)
                .build();

        TypeLoan typeLoan = new TypeLoan();
        typeLoan.setMinimumAmount(BigDecimal.valueOf(1000));
        typeLoan.setMaximumAmount(BigDecimal.valueOf(10000));
        typeLoan.setAutomaticValidation(true);

        when(typeLoanRepository.findById(typeLoanId)).thenReturn(Mono.just(typeLoan));
        when(orderRepository.save(order)).thenReturn(Mono.just(order));
        when(notificationRepository.sendCalculateCapacity(Mockito.any())).thenReturn(Mono.empty());
        when(orderRepository.findPendingOrders(
                Mockito.any(), Mockito.any(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(Flux.empty());

        StepVerifier.create(orderUseCase.saveOrder(order))
                .expectNextMatches(saved ->
                        saved.getAmount().equals(order.getAmount()) &&
                                saved.getIdTypeLoan().equals(order.getIdTypeLoan()))
                .verifyComplete();

        verify(orderRepository).save(order);
        verify(notificationRepository).sendCalculateCapacity(Mockito.any());
    }

}
