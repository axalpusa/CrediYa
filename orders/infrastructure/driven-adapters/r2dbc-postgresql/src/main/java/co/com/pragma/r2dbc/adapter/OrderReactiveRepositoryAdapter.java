package co.com.pragma.r2dbc.adapter;

import co.com.pragma.model.dto.OrderPendingDTO;
import co.com.pragma.model.order.Order;
import co.com.pragma.model.order.gateways.OrderRepository;
import co.com.pragma.r2dbc.adapter.interfaces.OrderReactiveRepository;
import co.com.pragma.r2dbc.entities.OrderEntity;
import co.com.pragma.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;


@Component
public class OrderReactiveRepositoryAdapter extends ReactiveAdapterOperations <
        Order, OrderEntity, UUID, OrderReactiveRepository
        > implements OrderRepository {

    private final DatabaseClient client;

    public OrderReactiveRepositoryAdapter(OrderReactiveRepository repository, ObjectMapper mapper, DatabaseClient client) {
        super ( repository, mapper, d -> mapper.mapBuilder ( d, Order.OrderBuilder.class ).build ( ) );
        this.client = client;
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

    @Override
    public Flux < OrderPendingDTO > findPendingOrders(String filterEmail, int page, int size) {

        String sql = """
                        
                        SELECT o.amount, o.term_months, o.email_address,
                       t.name AS loan_type, t.interest_rate,
                       s.name AS order_status,
                       COALESCE(SUM(
                               CASE
                                   WHEN s.id_status = '1603cbb9-f4ad-4112-9804-c3d4c04a48f5' THEN
                                       (o.amount * t.interest_rate) / (1 - POWER(1 + t.interest_rate, -o.term_months))
                                   ELSE 0
                               END
                           ) OVER (PARTITION BY o.email_address), 0) AS deuda_total_mensual_solicitudes_aprobadas
                FROM orders o
                         JOIN type_loan t ON o.id_type_loan = t.id_type_loan
                         JOIN status s ON o.id_status = s.id_status
                WHERE (:filterEmail IS NULL OR o.email_address LIKE :filterEmail)
                ORDER BY o.id_order DESC
                LIMIT :limit OFFSET :offset
                """;

        return client.sql(sql)
                .bind("filterEmail", filterEmail != null ? "%" + filterEmail + "%" : null)
                .bind("limit", size)
                .bind("offset", page * size)
                .map((row, metadata) -> OrderPendingDTO.builder()
                        .amount(row.get("amount", BigDecimal.class))
                        .termMonths(row.get("term_months", Integer.class))
                        .email (row.get("email_address", String.class))
                        .typeLoan (row.get("loan_type", String.class))
                        .interestRate(row.get("interest_rate", BigDecimal.class))
                        .statusOrder (row.get("order_status", String.class))
                        .build()
                )
                .all();
    }

    private Order toOrder(OrderEntity entity) {
        return mapper.map ( entity, Order.class );
    }
}
