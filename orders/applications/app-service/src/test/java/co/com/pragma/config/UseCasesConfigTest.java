package co.com.pragma.config;

import co.com.pragma.model.notification.gateways.NotificationRepository;
import co.com.pragma.model.order.gateways.OrderRepository;
import co.com.pragma.model.status.gateways.StatusRepository;
import co.com.pragma.model.typeloan.gateways.TypeLoanRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UseCasesConfigTest {

    @Test
    void testAllUseCaseBeansExist() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext ( TestConfig.class )) {
            String[] beanNames = context.getBeanDefinitionNames ( );

            long count = Arrays.stream ( beanNames )
                    .filter ( name -> name.endsWith ( "UseCase" ) )
                    .count ( );
            assertTrue ( count > 0, "No beans ending with 'Use Case' were found" );
        }
    }

    @Configuration
    @Import(UseCasesConfig.class)
    static class TestConfig {
        @Bean
        public OrderRepository orderRepository() {
            return Mockito.mock ( OrderRepository.class );
        }
        @Bean
        public TypeLoanRepository typeLoanRepository() {
            return Mockito.mock ( TypeLoanRepository.class );
        }
        @Bean
        public StatusRepository statusRepository() {
            return Mockito.mock ( StatusRepository.class );
        }
        @Bean
        public NotificationRepository notificationRepository() {
            return Mockito.mock ( NotificationRepository.class );
        }
    }
}