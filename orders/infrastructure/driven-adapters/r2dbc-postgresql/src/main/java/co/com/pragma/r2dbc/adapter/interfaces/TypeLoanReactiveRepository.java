package co.com.pragma.r2dbc.adapter.interfaces;

import co.com.pragma.r2dbc.entities.TypeLoanEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface TypeLoanReactiveRepository extends ReactiveCrudRepository < TypeLoanEntity, UUID >, ReactiveQueryByExampleExecutor < TypeLoanEntity > {
}
