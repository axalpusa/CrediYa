package co.com.pragma.model.status.gateways;

import co.com.pragma.model.status.Status;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface StatusRepository {
    Mono < Status > save(Status user);

    Mono < Status > findById(UUID id);

    Mono < Void > deleteById(UUID id);

    Flux < Status > findAll();
}
