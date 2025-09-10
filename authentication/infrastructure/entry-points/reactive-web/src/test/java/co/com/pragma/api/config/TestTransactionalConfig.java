package co.com.pragma.api.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.reactive.TransactionalOperator;

@TestConfiguration
public class TestTransactionalConfig {

    @Bean
    public TransactionalOperator transactionalOperator() {
        return Mockito.mock(TransactionalOperator.class);
    }
}
