package co.com.pragma.usecase.order;

import co.com.pragma.model.notification.gateways.NotificationRepository;
import co.com.pragma.model.order.Order;
import co.com.pragma.model.order.OrderPending;
import co.com.pragma.model.order.gateways.OrderRepository;
import co.com.pragma.model.typeloan.gateways.TypeLoanRepository;
import constants.Constants;
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

    public Mono<Order> saveOrder(Order order) {
        return validateOrder(order)
                .then(typeLoanRepository.findById(order.getIdTypeLoan())
                        .switchIfEmpty(Mono.error(new IllegalArgumentException( Constants.REQUEST_EMPTY)))
                )
                .flatMap(typeLoan -> {
                    if (order.getAmount().compareTo(typeLoan.getMinimumAmount()) < 0 ||
                            order.getAmount().compareTo(typeLoan.getMaximumAmount()) > 0) {
                        return Mono.error(new IllegalArgumentException("The amount is not within the allowed range"));
                    }

                    return orderRepository.save(order);
                });
    }

    private Mono<Void> validateOrder(Order order) {
        List<String> errors = new ArrayList<>();

        if (isBlank(order.getDocumentId())) errors.add("Document id is required.");
        if (isBlank(order.getEmailAddress())) errors.add("Email address is required.");
        if (order.getTermMonths() == null || order.getTermMonths() < 0) errors.add("Term months is required.");
        if (order.getAmount() == null || order.getAmount().doubleValue() < 0)
            errors.add("Amount is required");

        return errors.isEmpty() ? Mono.empty() : Mono.error(new ValidationPragmaException (errors));
    }

    public Mono<Order> updateOrder(Order order) {
        if (order == null) return Mono.error(new IllegalArgumentException(Constants.ORDER_NOT_FOUND));
        return orderRepository.save(order);
    }

    public Mono<Order> getOrderById(UUID id) {
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new ValidationPragmaException (
                        List.of( Constants.ORDER_NOT_FOUND + id)
                )));
    }

    public Mono<Void> deleteOrderById(UUID id) {
        return orderRepository.deleteById(id);
    }

    public Flux<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Flux<OrderPending> findPendingOrders(UUID filterStatus, int page, int size) {
        return orderRepository.findPendingOrders(filterStatus, page, size);
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
