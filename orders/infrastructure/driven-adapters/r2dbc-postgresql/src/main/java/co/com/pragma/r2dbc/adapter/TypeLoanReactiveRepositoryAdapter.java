package co.com.pragma.r2dbc.adapter;

import co.com.pragma.model.typeloan.TypeLoan;
import co.com.pragma.model.typeloan.gateways.TypeLoanRepository;
import co.com.pragma.r2dbc.adapter.interfaces.TypeLoanReactiveRepository;
import co.com.pragma.r2dbc.entities.TypeLoanEntity;
import co.com.pragma.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class TypeLoanReactiveRepositoryAdapter extends ReactiveAdapterOperations <
        TypeLoan, TypeLoanEntity, UUID, TypeLoanReactiveRepository
        > implements TypeLoanRepository {
    public TypeLoanReactiveRepositoryAdapter(TypeLoanReactiveRepository repository, ObjectMapper mapper) {
        super ( repository, mapper, d -> mapper.mapBuilder ( d, TypeLoan.TypeLoanBuilder.class ).build ( ) );
    }

    @Override
    @Transactional
    public Mono < TypeLoan > save(TypeLoan typeLoan) {
        TypeLoanEntity entity = mapper.map ( typeLoan, TypeLoanEntity.class );
        return repository.save ( entity )
                .map ( saved -> mapper.map ( saved, TypeLoan.class ) );
    }

    @Override
    public Flux < TypeLoan > findAll() {
        return repository.findAll ( )
                .map ( this::toTypeLoan );
    }

    @Override
    public Mono < TypeLoan > findById(UUID id) {
        return super.findById ( id );
    }

    @Override
    public Mono < Void > deleteById(UUID id) {
        return repository.deleteById ( id );
    }

    private TypeLoan toTypeLoan(TypeLoanEntity entity) {
        return mapper.map ( entity, TypeLoan.class );
    }
}
