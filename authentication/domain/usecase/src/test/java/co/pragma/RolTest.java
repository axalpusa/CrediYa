package co.pragma;

import co.com.pragma.model.rol.Rol;
import co.com.pragma.model.rol.gateways.RolRepository;
import co.com.pragma.usecase.rol.RolUseCase;
import exceptions.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RolTest {
    private RolRepository rolRepository;
    private RolUseCase rolUseCase;

    @BeforeEach
    void setUp() {
        rolRepository = Mockito.mock ( RolRepository.class );
        rolUseCase = new RolUseCase ( rolRepository );
    }

    @Test
    void shouldReturnRolWhenExists() {
        UUID idRolRol = UUID.fromString ( "a71e243b-e901-4e6e-b521-85ff39ac2f3e" );
        UUID id = UUID.randomUUID ( );
        Rol rol = new Rol ( );
        rol.setName ( "Rol" );
        rol.setDescription ( "Des" );
        rol.setIdRol ( idRolRol );
    
        when ( rolRepository.findById ( id ) ).thenReturn ( Mono.just ( rol ) );

        StepVerifier.create(rolUseCase.getRolById(id))
                .expectNextMatches(u -> u.getName().equals("Rol") &&
                        u.getIdRol().equals(idRolRol))
                .verifyComplete();

        verify ( rolRepository ).findById ( id );
    }


    @Test
    void shouldThrowExceptionWhenRolNotFound() {
        UUID id = UUID.randomUUID ( );

        when ( rolRepository.findById ( id ) ).thenReturn ( Mono.empty ( ) );

        StepVerifier.create(rolUseCase.getRolById(id))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException &&
                        throwable.getMessage().contains(id.toString()))
                .verify();

        verify ( rolRepository ).findById ( id );
    }
    @Test
    void shouldSaveRolSuccessfully() {
        Rol rol = new Rol();
        rol.setName("Rol");
        rol.setDescription("Des");

        when(rolRepository.save(rol)).thenReturn(Mono.just(rol));

        StepVerifier.create(rolUseCase.saveRol(rol))
                .expectNextMatches(u ->  u.getName().equals("Rol"))
                .verifyComplete();

        verify(rolRepository).save(rol);
    }


    @Test
    void shouldUpdateRolSuccessfully() {
        UUID id = UUID.randomUUID();
        Rol existing = new Rol();
        existing.setIdRol(id);
        existing.setName("Rol");

        Rol updated = new Rol();
        updated.setIdRol(id);
        updated.setName("Rol1");

        when(rolRepository.findById(id)).thenReturn(Mono.just(existing));
        when(rolRepository.save(existing)).thenReturn(Mono.just(updated));

        StepVerifier.create(rolUseCase.updateRol(updated))
                .expectNextMatches(u -> u.getName().equals("Rol1"))
                .verifyComplete();

        verify(rolRepository).findById(id);
        verify(rolRepository).save(existing);
    }

    @Test
    void shouldDeleteRolSuccessfully() {
        UUID id = UUID.randomUUID();

        when(rolRepository.deleteById(id)).thenReturn(Mono.empty());

        StepVerifier.create(rolUseCase.deleteRolById(id))
                .verifyComplete();

        verify(rolRepository).deleteById(id);
    }

    @Test
    void shouldGetAllRolsSuccessfully() {
        Rol rol = new Rol();
        rol.setName("Rol");

        when(rolRepository.findAll()).thenReturn(reactor.core.publisher.Flux.just(rol));

        StepVerifier.create(rolUseCase.getAllRol())
                .expectNextMatches(u -> u.getName().equals("Rol"))
                .verifyComplete();

        verify(rolRepository).findAll();
    }
    
   
}
