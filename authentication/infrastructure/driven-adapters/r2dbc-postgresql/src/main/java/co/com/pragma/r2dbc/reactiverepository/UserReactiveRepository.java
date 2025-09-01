package co.com.pragma.r2dbc.reactiverepository;

import co.com.pragma.r2dbc.entities.UserEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public interface UserReactiveRepository extends ReactiveCrudRepository < UserEntity, UUID >, ReactiveQueryByExampleExecutor < UserEntity > {

    Mono < UserEntity > findByEmailAddress(String emailAddress);
}
