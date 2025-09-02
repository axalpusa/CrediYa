package co.pragma;

import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
import co.com.pragma.usecase.user.UserUseCase;
import exceptions.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserTest {
    private UserRepository userRepository;
    private UserUseCase userUseCase;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock ( UserRepository.class );
        userUseCase = new UserUseCase ( userRepository );
    }

    @Test
    void shouldReturnUserWhenExists() {
        UUID idRolUser = UUID.fromString ( "a71e243b-e901-4e6e-b521-85ff39ac2f3e" );
        UUID id = UUID.randomUUID ( );
        User user = new User ( );
        user.setFirstName ( "axel" );
        user.setLastName ( "Puertas" );
        user.setAddress ( "Av santa rosa" );
        user.setEmailAddress ( "axalpusa11125@gmail.com" );
        user.setBirthDate ( LocalDate.parse ( "01-05-1994", DateTimeFormatter.ofPattern ( "dd-MM-yyyy" ) ) );
        user.setDocumentId ( "48594859" );
        user.setPhoneNumber ( "973157252" );
        user.setBaseSalary ( new BigDecimal ( "700000" ) );
        user.setPassword ( "$2a$10$mfILaHia4jqInB2mUQ2Vt.0PJxjJoXODUnkzchdHH6hxzPoF6xSjO" );
        user.setIdRol ( idRolUser );

        when ( userRepository.findById ( id ) ).thenReturn ( Mono.just ( user ) );

        StepVerifier.create(userUseCase.getUserById(id))
                .expectNextMatches(u -> u.getFirstName().equals("axel") &&
                        u.getIdRol().equals(idRolUser) &&
                        u.getEmailAddress().equals("axalpusa11125@gmail.com"))
                .verifyComplete();

        verify ( userRepository ).findById ( id );
    }


    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        UUID id = UUID.randomUUID ( );

        when ( userRepository.findById ( id ) ).thenReturn ( Mono.empty ( ) );

        StepVerifier.create(userUseCase.getUserById(id))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException &&
                        throwable.getMessage().contains(id.toString()))
                .verify();

        verify ( userRepository ).findById ( id );
    }

}
