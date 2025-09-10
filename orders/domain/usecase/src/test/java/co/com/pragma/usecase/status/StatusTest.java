package co.com.pragma.usecase.status;

import co.com.pragma.model.status.Status;
import co.com.pragma.model.status.gateways.StatusRepository;
import exceptions.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StatusTest {
    private StatusRepository statusRepository;
    private StatusUseCase statusUseCase;

    @BeforeEach
    void setUp() {
        statusRepository = Mockito.mock ( StatusRepository.class );
        statusUseCase = new StatusUseCase ( statusRepository );
    }

    @Test
    void shouldReturnStatusWhenExists() {
        UUID id = UUID.randomUUID ( );
        Status status = new Status ( );
        status.setName ( "Status" );
        status.setDescription ( "desc" );

        when ( statusRepository.findById ( id ) ).thenReturn ( Mono.just ( status ) );

        StepVerifier.create ( statusUseCase.getStatusById ( id ) )
                .expectNextMatches ( u -> u.getName ( ).equals ( "Status" )  )
                .verifyComplete ( );

        verify ( statusRepository ).findById ( id );
    }


    @Test
    void shouldThrowExceptionWhenStatusNotFound() {
        UUID id = UUID.randomUUID ( );

        when ( statusRepository.findById ( id ) ).thenReturn ( Mono.empty ( ) );

        StepVerifier.create ( statusUseCase.getStatusById ( id ) )
                .expectErrorMatches ( throwable -> throwable instanceof ValidationException &&
                        throwable.getMessage ( ).contains ( id.toString ( ) ) )
                .verify ( );

        verify ( statusRepository ).findById ( id );
    }

    @Test
    void shouldSaveStatusSuccessfully() {
        Status status = new Status ( );
        status.setName ( "Status" );
        status.setDescription ( "desc" );

        when ( statusRepository.save ( status ) ).thenReturn ( Mono.just ( status ) );

        StepVerifier.create ( statusUseCase.saveStatus ( status ) )
                .expectNextMatches ( u -> u.getName ( ).equals ( "Status" ) )
                .verifyComplete ( );

        verify ( statusRepository ).save ( status );
    }


    @Test
    void shouldUpdateStatusSuccessfully() {
        UUID id = UUID.randomUUID ( );
        Status existing = new Status ( );
        existing.setIdStatus ( id );
        existing.setName ( "Status" );
        existing.setDescription ( "desc" );

        Status updated = new Status ( );
        updated.setIdStatus ( id );
        updated.setName ( "Status1" );

        when ( statusRepository.findById ( id ) ).thenReturn ( Mono.just ( existing ) );
        when ( statusRepository.save ( existing ) ).thenReturn ( Mono.just ( updated ) );

        StepVerifier.create ( statusUseCase.updateStatus ( updated ) )
                .expectNextMatches ( u -> u.getName ( ).equals ( "Status1" ) )
                .verifyComplete ( );

        verify ( statusRepository ).findById ( id );
        verify ( statusRepository ).save ( existing );
    }

    @Test
    void shouldDeleteStatusSuccessfully() {
        UUID id = UUID.randomUUID ( );

        when ( statusRepository.deleteById ( id ) ).thenReturn ( Mono.empty ( ) );

        StepVerifier.create ( statusUseCase.deleteStatusById ( id ) )
                .verifyComplete ( );

        verify ( statusRepository ).deleteById ( id );
    }

    @Test
    void shouldGetAllTypesLoanSuccessfully() {
        Status status = new Status ( );
        status.setName ( "Status1" );

        when ( statusRepository.findAll ( ) ).thenReturn ( reactor.core.publisher.Flux.just ( status ) );

        StepVerifier.create ( statusUseCase.getAlStatus ( ) )
                .expectNextMatches ( u -> u.getName ( ).equals ( "Status1" ) )
                .verifyComplete ( );

        verify ( statusRepository ).findAll ( );
    }

    @Test
    void merge_shouldUpdateFields_whenOtherHasValues() {
        UUID id = UUID.randomUUID ( );
        Status original = Status.builder ( )
                .idStatus (id )
                .name ( "Status" )
                .description ( "desc" )
                .build ( );

        Status other = Status.builder ( )
                .name ( "Status1" )
                .build ( );

        original.merge ( other );

        assertThat ( original.getName ( ) ).isEqualTo ( "Status1" );
        assertThat ( original.getDescription ( ) ).isEqualTo ( "desc" );
    }

    @Test
    void merge_shouldNotUpdate_whenOtherHasNullFields() {
        UUID id = UUID.randomUUID ( );
        Status original = Status.builder ( )
                .idStatus ( id )
                .name ( "Status" )
                .description ( "desc" )
                .build ( );

        Status other = new Status ( );

        original.merge ( other );

        assertThat ( original.getName ( ) ).isEqualTo ( "Status" );
        assertThat ( original.getDescription ( ) ).isEqualTo ( "desc" );
    }

    @Test
    void builder_shouldCreateStatusWithCorrectValues() {
        UUID id = UUID.randomUUID ( );
        Status status = Status.builder ( )
                .idStatus ( id )
                .name ( "Status" )
                .description ( "desc" )
                .build ( );

        assertThat ( status.getIdStatus ( ) ).isEqualTo ( id );
        assertThat ( status.getName ( ) ).isEqualTo ( "Status" );
        assertThat ( status.getDescription ( ) ).isEqualTo ( "desc" );
    }


}
