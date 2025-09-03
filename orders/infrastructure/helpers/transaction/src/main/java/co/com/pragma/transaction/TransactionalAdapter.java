package co.com.pragma.transaction;

import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class TransactionalAdapter {
    private final TransactionalOperator transactionalOperator;

    public TransactionalAdapter(TransactionalOperator transactionalOperator) {
        this.transactionalOperator = transactionalOperator;
    }

    public < T > Mono < T > executeInTransaction(Mono < T > publisher) {
        return transactionalOperator.transactional ( publisher );
    }

    public < T > Flux < T > executeInTransaction(Flux < T > publisher) {
        return transactionalOperator.transactional ( publisher );
    }
}
