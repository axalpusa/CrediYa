package co.com.pragma.model.order.gateways;

import co.com.pragma.model.order.Order;
import co.com.pragma.model.order.OrderPending;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;


public interface OrderRepository {
    Mono < Order > save(Order order);

    Flux < Order > findAll();

    Mono < Order > findById(UUID id);

    Mono < Void > deleteById(UUID id);

    Flux < OrderPending > findPendingOrders(UUID filterStatus, String filterEmail, int page, int size);
}
