package co.com.pragma.model.order.gateways;

import co.com.pragma.model.order.Order;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Interface for operations the persistence
 */
public interface OrderRepository {
    Mono < Order > save(Order order);

    Flux < Order > findAll();

    Mono < Order > findById(UUID id);

    Mono < Void > deleteById(UUID id);


}
