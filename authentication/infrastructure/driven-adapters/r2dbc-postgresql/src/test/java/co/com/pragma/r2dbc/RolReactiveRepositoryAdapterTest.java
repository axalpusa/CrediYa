package co.com.pragma.r2dbc;

import co.com.pragma.model.rol.Rol;
import co.com.pragma.r2dbc.entities.RolEntity;
import co.com.pragma.r2dbc.interfaces.RolReactiveRepository;
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

class RolReactiveRepositoryAdapterTest {

    private RolReactiveRepository repository;
    private ObjectMapper mapper;
    private RolReactiveRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock ( RolReactiveRepository.class );
        mapper = mock ( ObjectMapper.class );
        adapter = new RolReactiveRepositoryAdapter ( repository, mapper );
    }

    @Test
    void shouldSaveRol() {
        Rol rol = buildRol ( );
        RolEntity entity = buildRolEntity ( );

        when ( mapper.map ( rol, RolEntity.class ) ).thenReturn ( entity );
        when ( repository.save ( entity ) ).thenReturn ( Mono.just ( entity ) );
        when ( mapper.map ( entity, Rol.class ) ).thenReturn ( rol );

        StepVerifier.create ( adapter.save ( rol ) )
                .expectNext ( rol )
                .verifyComplete ( );

        verify ( repository ).save ( entity );
        verify ( mapper ).map ( rol, RolEntity.class );
        verify ( mapper ).map ( entity, Rol.class );
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
        Rol rol = buildRol ( );
        RolEntity entity = buildRolEntity ( );

        when ( repository.findAll ( ) ).thenReturn ( Flux.just ( entity ) );
        when ( mapper.map ( entity, Rol.class ) ).thenReturn ( rol );

        StepVerifier.create ( adapter.findAll ( ) )
                .expectNext ( rol )
                .verifyComplete ( );

        verify ( repository ).findAll ( );
        verify ( mapper ).map ( entity, Rol.class );
    }

    private Rol buildRol() {
        return Rol.builder ( )
                .idRol ( UUID.randomUUID ( ) )
                .name ( "rol 1" )
                .description ( "describe rol 1" )
                .build ( );
    }

    private RolEntity buildRolEntity() {
        RolEntity entity = new RolEntity ( );
        entity.setIdRol ( UUID.randomUUID ( ) );
        entity.setName ( "rol 1" );
        entity.setDescription ( "describe rol 1" );
        return entity;
    }

}
