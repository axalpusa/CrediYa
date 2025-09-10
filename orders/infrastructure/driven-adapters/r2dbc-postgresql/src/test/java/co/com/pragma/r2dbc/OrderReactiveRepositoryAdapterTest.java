package co.com.pragma.r2dbc;

import co.com.pragma.model.order.Order;
import co.com.pragma.r2dbc.adapter.OrderReactiveRepositoryAdapter;
import co.com.pragma.r2dbc.adapter.interfaces.OrderReactiveRepository;
import co.com.pragma.r2dbc.entities.OrderEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderReactiveRepositoryAdapterTest {

    private OrderReactiveRepository repository;
    private ObjectMapper mapper;
    private OrderReactiveRepositoryAdapter adapter;
    private DatabaseClient client;

    @BeforeEach
    void setUp() {
        repository = mock ( OrderReactiveRepository.class );
        mapper = mock ( ObjectMapper.class );
        client = mock ( DatabaseClient.class );
        adapter = new OrderReactiveRepositoryAdapter ( repository, mapper, client );
    }

    @Test
    void shouldSaveRol() {
        Order rol = buildOrder ( );
        OrderEntity entity = buildOrderEntity ( );

        when ( mapper.map ( rol, OrderEntity.class ) ).thenReturn ( entity );
        when ( repository.save ( entity ) ).thenReturn ( Mono.just ( entity ) );
        when ( mapper.map ( entity, Order.class ) ).thenReturn ( rol );

        StepVerifier.create ( adapter.save ( rol ) )
                .expectNext ( rol )
                .verifyComplete ( );

        verify ( repository ).save ( entity );
        verify ( mapper ).map ( rol, OrderEntity.class );
        verify ( mapper ).map ( entity, Order.class );
    }


    @Test
    void shouldDeleteById() {
        UUID id = UUID.randomUUID ( );
        when ( repository.deleteById ( id ) ).thenReturn ( Mono.empty ( ) );

        StepVerifier.create ( adapter.deleteById ( id ) )
                .verifyComplete ( );

        verify ( repository ).deleteById ( id );
    }

    @Test
    void shouldFindAllRols() {
        Order order = buildOrder ( );
        OrderEntity entity = buildOrderEntity ( );

        when ( repository.findAll ( ) ).thenReturn ( Flux.just ( entity ) );
        when ( mapper.map ( entity, Order.class ) ).thenReturn ( order );

        StepVerifier.create ( adapter.findAll ( ) )
                .expectNext ( order )
                .verifyComplete ( );

        verify ( repository ).findAll ( );
        verify ( mapper ).map ( entity, Order.class );
    }

    private Order buildOrder() {
        return Order.builder ( )
                .idOrder ( UUID.randomUUID ( ) )
                .idTypeLoan ( UUID.randomUUID ( ) )
                .amount ( new BigDecimal ( 10000.00 ) )
                .idStatus ( UUID.randomUUID ( ) )
                .documentId ( "48295730" )
                .emailAddress ( "axalpusa@gmail.com" )
                .termMonths ( 12 )
                .build ( );
    }

    private OrderEntity buildOrderEntity() {
        OrderEntity entity = new OrderEntity ( );
        entity.setIdOrder ( UUID.randomUUID ( ) );
        entity.setIdTypeLoan ( UUID.randomUUID ( ) );
        entity.setAmount ( new BigDecimal ( 10000.00 ) );
        entity.setIdStatus ( UUID.randomUUID ( ) );
        entity.setDocumentId ( "d48295730" );
        entity.setEmailAddress ( "axalpusa1125@gmail.com" );
        entity.setTermMonths ( 12 );
        return entity;
    }

}
