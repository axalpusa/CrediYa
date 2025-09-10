package co.com.pragma.r2dbc;

import co.com.pragma.model.status.Status;
import co.com.pragma.r2dbc.adapter.StatusReactiveRepositoryAdapter;
import co.com.pragma.r2dbc.adapter.interfaces.StatusReactiveRepository;
import co.com.pragma.r2dbc.entities.StatusEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StatusReactiveRepositoryAdapterTest {

    private StatusReactiveRepository repository;
    private ObjectMapper mapper;
    private StatusReactiveRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock ( StatusReactiveRepository.class );
        mapper = mock ( ObjectMapper.class );
        adapter = new StatusReactiveRepositoryAdapter ( repository, mapper );
    }

    @Test
    void shouldSaveRol() {
        Status status = buildStatus ( );
        StatusEntity entity = buildEstatusEntity ( );

        when ( mapper.map ( status, StatusEntity.class ) ).thenReturn ( entity );
        when ( repository.save ( entity ) ).thenReturn ( Mono.just ( entity ) );
        when ( mapper.map ( entity, Status.class ) ).thenReturn ( status );

        StepVerifier.create ( adapter.save ( status ) )
                .expectNext ( status )
                .verifyComplete ( );

        verify ( repository ).save ( entity );
        verify ( mapper ).map ( status, StatusEntity.class );
        verify ( mapper ).map ( entity, Status.class );
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
        Status status = buildStatus ( );
        StatusEntity entity = buildEstatusEntity ( );

        when ( repository.findAll ( ) ).thenReturn ( Flux.just ( entity ) );
        when ( mapper.map ( entity, Status.class ) ).thenReturn ( status );

        StepVerifier.create ( adapter.findAll ( ) )
                .expectNext ( status )
                .verifyComplete ( );

        verify ( repository ).findAll ( );
        verify ( mapper ).map ( entity, Status.class );
    }

    private Status buildStatus() {
        return Status.builder ( )
                .idStatus ( UUID.randomUUID ( ) )
                .name ( "status" )
                .description ( "desc" )
                .build ( );
    }

    private StatusEntity buildEstatusEntity() {
        StatusEntity entity = new StatusEntity ( );
        entity.setIdStatus ( UUID.randomUUID ( ) );
        entity.setName ( "status" );
        entity.setDescription ( "desc" );
        return entity;
    }

}
