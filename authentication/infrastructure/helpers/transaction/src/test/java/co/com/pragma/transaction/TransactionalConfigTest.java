package co.com.pragma.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.transaction.reactive.TransactionalOperator;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class TransactionalConfigTest {

    private TransactionalConfig transactionalConfig;
    private TransactionalOperator transactionalOperator;

    @BeforeEach
    void setUp() {
        transactionalOperator = Mockito.mock(TransactionalOperator.class);
        transactionalConfig = new TransactionalConfig();
    }

    @Test
    void transactionalAdapter_shouldReturnNonNull() {
        TransactionalAdapter adapter = transactionalConfig.transactionalAdapter(transactionalOperator);

        assertNotNull(adapter);
    }
}
