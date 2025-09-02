package co.com.pragma.r2dbc.adapter;

import co.com.pragma.model.order.Order;
import co.com.pragma.model.order.gateways.OrderRepository;
import co.com.pragma.r2dbc.adapter.interfaces.OrderReactiveRepository;
import co.com.pragma.r2dbc.entities.OrderEntity;
import co.com.pragma.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;


@Component
public class OrderReactiveRepositoryAdapter extends ReactiveAdapterOperations <
        Order, OrderEntity, UUID, OrderReactiveRepository
        > implements OrderRepository {
    public OrderReactiveRepositoryAdapter(OrderReactiveRepository repository, ObjectMapper mapper) {
        super ( repository, mapper, d -> mapper.mapBuilder ( d, Order.OrderBuilder.class ).build ( ) );
    }


    @Override
    @Transactional
    public Mono < Order > save(Order order) {
        OrderEntity entity = mapper.map ( order, OrderEntity.class );
        return repository.save ( entity )
                .map ( saved -> mapper.map ( saved, Order.class ) );
    }

    @Override
    public Flux < Order > findAll() {
        return repository.findAll ( )
                .map ( this::toOrder );
    }

    @Override
    public Mono < Order > findById(UUID id) {
        return super.findById ( id );
    }


    @Override
    public Mono < Void > deleteById(UUID id) {
        return repository.deleteById ( id );
    }

    private Order toOrder(OrderEntity entity) {
        return mapper.map ( entity, Order.class );
    }
}
