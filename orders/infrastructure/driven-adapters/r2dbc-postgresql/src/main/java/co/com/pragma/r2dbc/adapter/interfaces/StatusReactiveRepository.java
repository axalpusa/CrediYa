package co.com.pragma.r2dbc.adapter.interfaces;

import co.com.pragma.r2dbc.entities.StatusEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface StatusReactiveRepository extends ReactiveCrudRepository < StatusEntity, UUID >, ReactiveQueryByExampleExecutor < StatusEntity > {

}
