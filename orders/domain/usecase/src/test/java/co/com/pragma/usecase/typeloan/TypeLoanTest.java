package co.com.pragma.usecase.typeloan;

import co.com.pragma.model.typeloan.TypeLoan;
import co.com.pragma.model.typeloan.gateways.TypeLoanRepository;
import exceptions.ValidationPragmaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TypeLoanTest {
    private TypeLoanRepository typeLoanRepository;
    private TypeLoanUseCase typeLoanUseCase;

    @BeforeEach
    void setUp() {
        typeLoanRepository = Mockito.mock ( TypeLoanRepository.class );
        typeLoanUseCase = new TypeLoanUseCase ( typeLoanRepository );
    }

    @Test
    void shouldReturnTypeLoanWhenExists() {
        UUID id = UUID.randomUUID ( );
        TypeLoan typeLoan = new TypeLoan ( );
        typeLoan.setName ( "Type" );
        typeLoan.setMaximumAmount ( BigDecimal.valueOf ( 1000.00 ) );
        typeLoan.setMinimumAmount ( BigDecimal.valueOf ( 500.00 ) );
        typeLoan.setInterestRate ( BigDecimal.valueOf ( 0.5 ) );
        typeLoan.setAutomaticValidation ( true );

        when ( typeLoanRepository.findById ( id ) ).thenReturn ( Mono.just ( typeLoan ) );

        StepVerifier.create ( typeLoanUseCase.getTypeLoanById ( id ) )
                .expectNextMatches ( u -> u.getName ( ).equals ( "Type" ) )
                .verifyComplete ( );

        verify ( typeLoanRepository ).findById ( id );
    }


    @Test
    void shouldThrowExceptionWhenTypeLoanNotFound() {
        UUID id = UUID.randomUUID ( );

        when ( typeLoanRepository.findById ( id ) ).thenReturn ( Mono.empty ( ) );

        StepVerifier.create ( typeLoanUseCase.getTypeLoanById ( id ) )
                .expectErrorMatches ( throwable -> throwable instanceof ValidationPragmaException &&
                        throwable.getMessage ( ).contains ( id.toString ( ) ) )
                .verify ( );

        verify ( typeLoanRepository ).findById ( id );
    }

    @Test
    void shouldSaveTypeLoanSuccessfully() {
        TypeLoan typeLoan = new TypeLoan ( );
        typeLoan.setName ( "Type" );
        typeLoan.setMaximumAmount ( BigDecimal.valueOf ( 1000.00 ) );
        typeLoan.setMinimumAmount ( BigDecimal.valueOf ( 500.00 ) );
        typeLoan.setInterestRate ( BigDecimal.valueOf ( 0.5 ) );
        typeLoan.setAutomaticValidation ( true );

        when ( typeLoanRepository.save ( typeLoan ) ).thenReturn ( Mono.just ( typeLoan ) );

        StepVerifier.create ( typeLoanUseCase.saveTypeLoan ( typeLoan ) )
                .expectNextMatches ( u -> u.getName ( ).equals ( "Type" ) )
                .verifyComplete ( );

        verify ( typeLoanRepository ).save ( typeLoan );
    }


    @Test
    void shouldUpdateTypeLoanSuccessfully() {
        UUID id = UUID.randomUUID ( );
        TypeLoan existing = new TypeLoan ( );
        existing.setName ( "Type" );
        existing.setMaximumAmount ( BigDecimal.valueOf ( 1000.00 ) );
        existing.setMinimumAmount ( BigDecimal.valueOf ( 500.00 ) );
        existing.setInterestRate ( BigDecimal.valueOf ( 0.5 ) );
        existing.setAutomaticValidation ( true );

        TypeLoan updated = new TypeLoan ( );
        updated.setIdTypeLoan ( id );
        updated.setName ( "Type1" );

        when ( typeLoanRepository.findById ( id ) ).thenReturn ( Mono.just ( existing ) );
        when ( typeLoanRepository.save ( existing ) ).thenReturn ( Mono.just ( updated ) );

        StepVerifier.create ( typeLoanUseCase.updateTypeLoan ( updated ) )
                .expectNextMatches ( u -> u.getName ( ).equals ( "Type1" ) )
                .verifyComplete ( );

        verify ( typeLoanRepository ).findById ( id );
        verify ( typeLoanRepository ).save ( existing );
    }

    @Test
    void shouldDeleteTypeLoanSuccessfully() {
        UUID id = UUID.randomUUID ( );

        when ( typeLoanRepository.deleteById ( id ) ).thenReturn ( Mono.empty ( ) );

        StepVerifier.create ( typeLoanUseCase.deleteTypeLoanById ( id ) )
                .verifyComplete ( );

        verify ( typeLoanRepository ).deleteById ( id );
    }

    @Test
    void shouldGetAllTypesLoanSuccessfully() {
        TypeLoan typeLoan = new TypeLoan ( );
        typeLoan.setName ( "Type1" );

        when ( typeLoanRepository.findAll ( ) ).thenReturn ( reactor.core.publisher.Flux.just ( typeLoan ) );

        StepVerifier.create ( typeLoanUseCase.getAllTypesLoan ( ) )
                .expectNextMatches ( u -> u.getName ( ).equals ( "Type1" ) )
                .verifyComplete ( );

        verify ( typeLoanRepository ).findAll ( );
    }

    @Test
    void merge_shouldUpdateFields_whenOtherHasValues() {
        UUID id = UUID.randomUUID ( );
        TypeLoan original = TypeLoan.builder ( )
                .idTypeLoan ( id )
                .name ( "Type" )
                .maximumAmount ( BigDecimal.valueOf ( 1000.00 ) )
                .minimumAmount ( BigDecimal.valueOf ( 500.00 ) )
                .interestRate ( BigDecimal.valueOf ( 0.5 ) )
                .automaticValidation ( true )
                .build ( );

        TypeLoan other = TypeLoan.builder ( )
                .name ( "Type1" )
                .build ( );

        original.merge ( other );

        assertThat ( original.getName ( ) ).isEqualTo ( "Type1" );
        assertThat ( original.getMaximumAmount ( ) ).isEqualTo ( BigDecimal.valueOf ( 1000.00 ) );
        assertThat ( original.getMinimumAmount ( ) ).isEqualTo ( BigDecimal.valueOf ( 500.00 ) );
        assertThat ( original.getInterestRate ( ) ).isEqualTo ( BigDecimal.valueOf ( 0.5 ) );
        assertThat ( original.getAutomaticValidation ( ) ).isEqualTo ( true );
    }

    @Test
    void merge_shouldNotUpdate_whenOtherHasNullFields() {
        UUID id = UUID.randomUUID ( );
        TypeLoan original = TypeLoan.builder ( )
                .idTypeLoan ( id )
                .name ( "Type" )
                .maximumAmount ( BigDecimal.valueOf ( 1000.00 ) )
                .minimumAmount ( BigDecimal.valueOf ( 500.00 ) )
                .interestRate ( BigDecimal.valueOf ( 0.5 ) )
                .automaticValidation ( true )
                .build ( );

        TypeLoan other = new TypeLoan ( );

        original.merge ( other );

        assertThat ( original.getName ( ) ).isEqualTo ( "Type" );
        assertThat ( original.getMaximumAmount ( ) ).isEqualTo ( BigDecimal.valueOf ( 1000.00 ) );
        assertThat ( original.getMinimumAmount ( ) ).isEqualTo ( BigDecimal.valueOf ( 500.00 ) );
        assertThat ( original.getInterestRate ( ) ).isEqualTo ( BigDecimal.valueOf ( 0.5 ) );
        assertThat ( original.getAutomaticValidation ( ) ).isEqualTo ( true );
    }

    @Test
    void builder_shouldCreateTypeLoanWithCorrectValues() {
        UUID id = UUID.randomUUID ( );
        TypeLoan typeLoan = TypeLoan.builder ( )
                .idTypeLoan ( id )
                .name ( "Type" )
                .maximumAmount ( BigDecimal.valueOf ( 1000.00 ) )
                .minimumAmount ( BigDecimal.valueOf ( 500.00 ) )
                .interestRate ( BigDecimal.valueOf ( 0.5 ) )
                .automaticValidation ( true )
                .build ( );

        assertThat ( typeLoan.getIdTypeLoan ( ) ).isEqualTo ( id );
        assertThat ( typeLoan.getName ( ) ).isEqualTo ( "Type" );
        assertThat ( typeLoan.getMaximumAmount ( ) ).isEqualTo ( BigDecimal.valueOf ( 1000.00 ) );
        assertThat ( typeLoan.getMinimumAmount ( ) ).isEqualTo ( BigDecimal.valueOf ( 500.00 ) );
        assertThat ( typeLoan.getInterestRate ( ) ).isEqualTo ( BigDecimal.valueOf ( 0.5 ) );
        assertThat ( typeLoan.getAutomaticValidation ( ) ).isEqualTo ( true );
    }

    @Test
    void updateTypeLoan_shouldUpdateFieldsSuccessfully() {
        UUID typeLoanId = UUID.randomUUID();

        TypeLoan existing = new TypeLoan();
        existing.setIdTypeLoan(typeLoanId);
        existing.setName("Personal Loan");
        existing.setInterestRate(BigDecimal.valueOf(5.0));
        existing.setAutomaticValidation(false);
        existing.setMinimumAmount(BigDecimal.valueOf(1000));
        existing.setMaximumAmount(BigDecimal.valueOf(5000));

        TypeLoan updated = new TypeLoan();
        updated.setIdTypeLoan(typeLoanId);
        updated.setName("Updated Loan");
        updated.setInterestRate(BigDecimal.valueOf(7.5));
        updated.setAutomaticValidation(true);
        updated.setMinimumAmount(BigDecimal.valueOf(1500));
        updated.setMaximumAmount(BigDecimal.valueOf(6000));

        when(typeLoanRepository.findById(typeLoanId)).thenReturn(Mono.just(existing));
        when(typeLoanRepository.save(Mockito.any(TypeLoan.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        Mono<TypeLoan> result = typeLoanUseCase.updateTypeLoan(updated);

        StepVerifier.create(result)
                .assertNext(saved -> {
                    assertThat(saved.getIdTypeLoan()).isEqualTo(typeLoanId);
                    assertThat(saved.getName()).isEqualTo("Updated Loan");
                    assertThat(saved.getInterestRate()).isEqualTo(BigDecimal.valueOf(7.5));
                    assertThat(saved.getAutomaticValidation()).isTrue();
                    assertThat(saved.getMinimumAmount()).isEqualTo(BigDecimal.valueOf(1500));
                    assertThat(saved.getMaximumAmount()).isEqualTo(BigDecimal.valueOf(6000));
                })
                .verifyComplete();

        verify(typeLoanRepository).findById(typeLoanId);
        verify(typeLoanRepository).save(Mockito.any(TypeLoan.class));
    }

}
