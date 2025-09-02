package co.com.pragma.r2dbc.interfaces;

import co.com.pragma.r2dbc.entities.RolEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface RolReactiveRepository extends ReactiveCrudRepository < RolEntity, UUID >, ReactiveQueryByExampleExecutor < RolEntity > {

}
