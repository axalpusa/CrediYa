package co.com.pragma.transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionalAdapterTest {

    @Mock
    private TransactionalOperator transactionalOperator;

    @InjectMocks
    private TransactionalAdapter transactionalAdapter;

    @Test
    void mustWrapMonoInTransaction() {
        Mono<String> mono = Mono.just("test");

        when(transactionalOperator.transactional(mono)).thenReturn(mono);

        StepVerifier.create(transactionalAdapter.executeInTransaction(mono))
                .expectNext("test")
                .verifyComplete();

        verify(transactionalOperator).transactional(mono);
    }

    @Test
    void mustWrapFluxInTransaction() {
        Flux<Integer> flux = Flux.just(1, 2);
        when(transactionalOperator.transactional(flux)).thenReturn(flux);
        StepVerifier.create(transactionalAdapter.executeInTransaction(flux))
                .expectNext(1, 2)
                .verifyComplete();
        verify(transactionalOperator).transactional(flux);
    }
}
