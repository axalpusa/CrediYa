package co.com.pragma.model.user.gateways;

import co.com.pragma.model.user.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserRepository {
    Mono<User> save(User user);

    Mono<User> findById(UUID id);

    Mono<User> findByEmailAddress(String emailAddress);

    Mono<Void> deleteById(UUID id);

    Flux<User> findAll();

}
