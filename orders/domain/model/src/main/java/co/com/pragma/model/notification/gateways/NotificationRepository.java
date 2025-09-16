package co.com.pragma.model.notification.gateways;

import co.com.pragma.model.order.CalculateCapacity;
import co.com.pragma.model.order.Order;
import reactor.core.publisher.Mono;

public interface NotificationRepository {
    Mono<Void> sendOrderStatus(Order order);
    Mono<Void> sendCalculateCapacity(  CalculateCapacity calculateCapacityMono);
}
