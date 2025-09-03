package co.com.pragma.transaction;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.reactive.TransactionalOperator;

@Configuration
public class TransactionalConfig {
    @Bean
    public TransactionalAdapter transactionalAdapter(TransactionalOperator transactionalOperator) {
        return new TransactionalAdapter ( transactionalOperator );
    }
}
