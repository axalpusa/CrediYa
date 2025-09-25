package co.com.pragma.usecase.order;

import co.com.pragma.model.notification.gateways.NotificationRepository;
import co.com.pragma.model.order.CalculateCapacity;
import co.com.pragma.model.order.Order;
import co.com.pragma.model.order.OrderPending;
import co.com.pragma.model.order.ReportResponse;
import co.com.pragma.model.order.gateways.OrderRepository;
import co.com.pragma.model.typeloan.gateways.TypeLoanRepository;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
import enums.StatusEnum;
import exceptions.ValidationPragmaException;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static constants.Constants.*;

@AllArgsConstructor
public class OrderUseCase {
    private final OrderRepository orderRepository;
    private final TypeLoanRepository typeLoanRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public Mono < Order > saveOrder(Order order, String token) {
        return validateOrder ( order )
                .then ( typeLoanRepository.findById ( order.getIdTypeLoan ( ) )
                        .switchIfEmpty ( Mono.error ( new IllegalArgumentException ( REQUEST_EMPTY ) ) )
                )
                .flatMap ( typeLoan -> {
                    if ( order.getAmount ( ).compareTo ( typeLoan.getMinimumAmount ( ) ) < 0 ||
                            order.getAmount ( ).compareTo ( typeLoan.getMaximumAmount ( ) ) > 0 ) {
                        return Mono.error ( new IllegalArgumentException ( AMOUNT_OF_RANGE ) );
                    }
                    order.setIdStatus ( StatusEnum.REVISION.getId ( ) );
                    if ( typeLoan.getAutomaticValidation ( ) ) {
                        return sendToCapacityLambda ( order, token );
                    } else {
                        return orderRepository.save ( order );
                    }
                } );
    }

    private Mono < Order > sendToCapacityLambda(Order order, String token) {
        return orderRepository.save ( order )
                .flatMap ( savedOrder -> {
                    return findPendingOrdersWithUserData (
                            StatusEnum.APPROVED.getId ( ),
                            savedOrder.getEmailAddress ( ),
                            0, 10, token
                    )
                            .collectList ( )
                            .map ( ordersList -> {
                                CalculateCapacity dto = new CalculateCapacity ( );
                                dto.setOrder ( savedOrder );
                                dto.setReportResponses ( ordersList );

                                return dto;
                            } )
                            .flatMap ( notificationRepository::sendCalculateCapacity )
                            .thenReturn ( savedOrder );
                } );
    }

    public Flux < ReportResponse > findPendingOrdersWithUserData(UUID filterStatus,
                                                                 String filterEmail,
                                                                 int page,
                                                                 int size,
                                                                 String token) {

        return orderRepository.findPendingOrders ( filterStatus, filterEmail, page, size )
                .flatMap ( order ->
                        userRepository.findByEmail ( order.getEmail ( ), token )
                                .onErrorResume ( e -> Mono.empty ( ) )
                                .map ( user -> mapToReportDTO ( order, user ) )
                );
    }

    private ReportResponse mapToReportDTO(OrderPending order, User user) {
        ReportResponse report = new ReportResponse ( );
        report.setStatusOrder ( order.getStatusOrder ( ) );
        report.setAmount ( order.getAmount ( ) );
        report.setEmail ( user.getEmailAddress ( ) );
        report.setName ( user.getFirstName ( ) + " " + user.getLastName ( ) );
        report.setBaseSalary ( user.getBaseSalary ( ) );
        report.setTypeLoan ( order.getTypeLoan ( ) );
        report.setInterestRate ( order.getInterestRate ( ) );
        report.setTermMonths ( order.getTermMonths ( ) );
        report.setTotalMonthlyDebtApprovedRequests ( order.getTotalMonthlyDebtApprovedRequests ( ) );
        return report;
    }

    private Mono < Void > validateOrder(Order order) {
        List < String > errors = new ArrayList <> ( );

        if ( isBlank ( order.getDocumentId ( ) ) ) errors.add ( DOCUMENT_REQUIRED );
        if ( isBlank ( order.getEmailAddress ( ) ) ) errors.add ( EMAIL_REQUIRED );
        if ( order.getTermMonths ( ) == null || order.getTermMonths ( ) < 0 ) errors.add ( TERM_REQUIRED );
        if ( order.getAmount ( ) == null || order.getAmount ( ).doubleValue ( ) < 0 )
            errors.add ( AMOUNT_REQUIRED );

        return errors.isEmpty ( ) ? Mono.empty ( ) : Mono.error ( new ValidationPragmaException ( errors ) );
    }

    public Mono < Order > updateOrder(Order order) {
        if ( order == null ) return Mono.error ( new IllegalArgumentException ( ORDER_NOT_FOUND ) );
        return orderRepository.save ( order );
    }

    public Mono < Order > getOrderById(UUID id) {
        return orderRepository.findById ( id )
                .switchIfEmpty ( Mono.error ( new ValidationPragmaException (
                        List.of ( ORDER_NOT_FOUND + id )
                ) ) );
    }

    public Mono < Void > deleteOrderById(UUID id) {
        return orderRepository.deleteById ( id );
    }

    public Flux < Order > getAllOrders() {
        return orderRepository.findAll ( );
    }

    public Flux < OrderPending > findPendingOrders(UUID filterStatus, String filterEmail, int page, int size) {
        return orderRepository.findPendingOrders ( filterStatus, filterEmail, page, size );
    }

    public Mono < Order > updateOrderAndNotify(Order order) {
        return orderRepository.save ( order )
                .flatMap ( savedOrder -> notificationRepository.sendOrderStatus ( savedOrder )
                        .thenReturn ( savedOrder )
                );
    }

    private boolean isBlank(String value) {
        return value == null || value.trim ( ).isEmpty ( );
    }

}
