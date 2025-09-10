package co.com.pragma.r2dbc;

import co.com.pragma.model.typeloan.TypeLoan;
import co.com.pragma.r2dbc.adapter.TypeLoanReactiveRepositoryAdapter;
import co.com.pragma.r2dbc.adapter.interfaces.TypeLoanReactiveRepository;
import co.com.pragma.r2dbc.entities.TypeLoanEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TypeLoanReactiveRepositoryAdapterTest {

    private TypeLoanReactiveRepository repository;
    private ObjectMapper mapper;
    private TypeLoanReactiveRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock ( TypeLoanReactiveRepository.class );
        mapper = mock ( ObjectMapper.class );
        adapter = new TypeLoanReactiveRepositoryAdapter ( repository, mapper );
    }

    @Test
    void shouldSaveRol() {
        TypeLoan typeLoan = buildTypeLoan ( );
        TypeLoanEntity entity = buildTypeLoanEntity ( );

        when ( mapper.map ( typeLoan, TypeLoanEntity.class ) ).thenReturn ( entity );
        when ( repository.save ( entity ) ).thenReturn ( Mono.just ( entity ) );
        when ( mapper.map ( entity, TypeLoan.class ) ).thenReturn ( typeLoan );

        StepVerifier.create ( adapter.save ( typeLoan ) )
                .expectNext ( typeLoan )
                .verifyComplete ( );

        verify ( repository ).save ( entity );
        verify ( mapper ).map ( typeLoan, TypeLoanEntity.class );
        verify ( mapper ).map ( entity, TypeLoan.class );
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
        TypeLoan typeLoan = buildTypeLoan ( );
        TypeLoanEntity entity = buildTypeLoanEntity ( );

        when ( repository.findAll ( ) ).thenReturn ( Flux.just ( entity ) );
        when ( mapper.map ( entity, TypeLoan.class ) ).thenReturn ( typeLoan );

        StepVerifier.create ( adapter.findAll ( ) )
                .expectNext ( typeLoan )
                .verifyComplete ( );

        verify ( repository ).findAll ( );
        verify ( mapper ).map ( entity, TypeLoan.class );
    }

    private TypeLoan buildTypeLoan() {
        return TypeLoan.builder ( )
                .idTypeLoan ( UUID.randomUUID ( ) )
                .name ( "type 1" )
                .maximumAmount ( new BigDecimal ( 5000.00 ) )
                .minimumAmount ( new BigDecimal ( 100.00 ) )
                .interestRate ( new BigDecimal ( 0.5 ) )
                .automaticValidation ( false )
                .build ( );
    }

    private TypeLoanEntity buildTypeLoanEntity() {
        TypeLoanEntity entity = new TypeLoanEntity ( );
        entity.setIdTypeLoan ( UUID.randomUUID ( ) );
        entity.setName ( "type 1" );
        entity.setMaximumAmount ( new BigDecimal ( 5000.00 ) );
        entity.setMinimumAmount ( new BigDecimal ( 100.00 ) );
        entity.setInterestRate ( new BigDecimal ( 0.5 ) );
        entity.setAutomaticValidation ( true );
        return entity;
    }

}
