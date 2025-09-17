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
import constants.Constants;
import enums.StatusEnum;
import exceptions.ValidationPragmaException;
import lombok.AllArgsConstructor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class OrderUseCase {
    private final OrderRepository orderRepository;
    private final TypeLoanRepository typeLoanRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public Mono<Order> saveOrder(Order order, String token) {
        return validateOrder(order)
                .then(typeLoanRepository.findById(order.getIdTypeLoan())
                        .switchIfEmpty(Mono.error(new IllegalArgumentException(Constants.REQUEST_EMPTY)))
                )
                .flatMap(typeLoan -> {
                    if (order.getAmount().compareTo(typeLoan.getMinimumAmount()) < 0 ||
                            order.getAmount().compareTo(typeLoan.getMaximumAmount()) > 0) {
                        return Mono.error(new IllegalArgumentException("The amount is not within the allowed range"));
                    }
                    order.setIdStatus(StatusEnum.REVISION.getId());
                    if (typeLoan.getAutomaticValidation()) {
                        return sendToCapacityLambda(order, token);
                    } else {
                        return orderRepository.save(order);
                    }
                });
    }

    private Mono<Order> sendToCapacityLambda(Order order, String token) {
        Flux<ReportResponse> reportResponseFlux = findPendingOrdersWithUserData(
                StatusEnum.APPROVED.getId(),
                order.getEmailAddress(),
                0, 10, token
        ).doOnNext(report -> {
            System.out.println("📌 ReportResponse encontrado: " + report);
        });

        Mono<CalculateCapacity> calculateCapacityMono = reportResponseFlux
                .collectList()
                .map(ordersList -> {
                    System.out.println("📊 Lista de ReportResponses size=" + ordersList.size());
                    CalculateCapacity dto = new CalculateCapacity();
                    dto.setOrder(order);
                    dto.setReportResponses(ordersList);
                    return dto;
                });

        return orderRepository.save(order)
                .flatMap(savedOrder ->
                        calculateCapacityMono
                                .doOnNext(calc -> System.out.println("⚙️ CalculateCapacity preparado: " + calc))
                                .flatMap(notificationRepository::sendCalculateCapacity)
                                .thenReturn(savedOrder)
                );
    }


    public Flux<ReportResponse> findPendingOrdersWithUserData(UUID filterStatus,
                                                              String filterEmail,
                                                              int page,
                                                              int size,
                                                              String token) {
        System.out.println("🔎 Buscando órdenes pendientes con filtro: status=" + filterStatus +
                ", email=" + filterEmail + ", page=" + page + ", size=" + size);

        return orderRepository.findPendingOrders(filterStatus, filterEmail, page, size)
                .doOnNext(order -> System.out.println("📌 Orden encontrada: " + order))
                .flatMap(order ->
                        userRepository.findByEmail(order.getEmail(), token)
                                .doOnNext(user -> System.out.println("👤 Usuario encontrado: " + user))
                                .onErrorResume(e -> {
                                    System.out.println("⚠️ Error buscando usuario para email=" + order.getEmail() + ": " + e.getMessage());
                                    return Mono.empty();
                                })
                                .map(user -> {
                                    ReportResponse report = mapToReportDTO(order, user);
                                    System.out.println("✅ ReportResponse construido: " + report);
                                    return report;
                                })
                );
    }


    private ReportResponse mapToReportDTO(OrderPending order, User user) {
        ReportResponse report = new ReportResponse();
        report.setStatusOrder(order.getStatusOrder());
        report.setAmount(order.getAmount());
        report.setEmail(user.getEmailAddress());
        report.setName(user.getFirstName() + " " + user.getLastName());
        report.setBaseSalary(user.getBaseSalary());
        report.setTypeLoan(order.getTypeLoan());
        report.setInterestRate(order.getInterestRate());
        report.setTermMonths(order.getTermMonths());
        report.setTotalMonthlyDebtApprovedRequests(order.getTotalMonthlyDebtApprovedRequests());
        return report;
    }

    private Mono<Void> validateOrder(Order order) {
        List<String> errors = new ArrayList<>();

        if (isBlank(order.getDocumentId())) errors.add("Document id is required.");
        if (isBlank(order.getEmailAddress())) errors.add("Email address is required.");
        if (order.getTermMonths() == null || order.getTermMonths() < 0) errors.add("Term months is required.");
        if (order.getAmount() == null || order.getAmount().doubleValue() < 0)
            errors.add("Amount is required");

        return errors.isEmpty() ? Mono.empty() : Mono.error(new ValidationPragmaException(errors));
    }

    public Mono<Order> updateOrder(Order order) {
        if (order == null) return Mono.error(new IllegalArgumentException(Constants.ORDER_NOT_FOUND));
        return orderRepository.save(order);
    }

    public Mono<Order> getOrderById(UUID id) {
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new ValidationPragmaException(
                        List.of(Constants.ORDER_NOT_FOUND + id)
                )));
    }

    public Mono<Void> deleteOrderById(UUID id) {
        return orderRepository.deleteById(id);
    }

    public Flux<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Flux<OrderPending> findPendingOrders(UUID filterStatus, String filterEmail, int page, int size) {
        return orderRepository.findPendingOrders(filterStatus, filterEmail, page, size);
    }

    public Mono<Order> updateOrderAndNotify(Order order) {
        return orderRepository.save(order)
                .flatMap(savedOrder -> notificationRepository.sendOrderStatus(savedOrder)
                        .thenReturn(savedOrder)
                );
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

}
