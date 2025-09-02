package co.pragma;

import co.com.pragma.model.user.User;
import co.com.pragma.usecase.authentication.AuthUseCase;
import co.com.pragma.usecase.user.UserUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;
import java.util.function.BiFunction;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AuthTest {
    private UserUseCase userUseCase;
    private AuthUseCase authUseCase;

    @BeforeEach
    void setUp() {
        userUseCase = Mockito.mock ( UserUseCase.class );
        authUseCase = new AuthUseCase ( userUseCase );
    }

    @Test
    void shouldLoginSuccessfully() {
        UUID userId = UUID.randomUUID ( );
        UUID rolId = UUID.randomUUID ( );
        String email = "axalpusa11125@gmail.com";
        String rawPassword = "123456";
        String storedPassword = "hashedPassword";

        User user = new User ( );
        user.setIdUser ( userId );
        user.setIdRol ( rolId );
        user.setEmailAddress ( email );
        user.setPassword ( storedPassword );
        user.setFirstName ( "Axel" );

        when ( userUseCase.findByEmailAddress ( email ) ).thenReturn ( Mono.just ( user ) );

        BiFunction < String, String, Boolean > passwordMatches = (raw, stored) -> raw.equals ( "123456" ) && stored.equals ( storedPassword );
        BiFunction < UUID, UUID, String > tokenGenerator = (uid, rid) -> "fake-token";

        StepVerifier.create ( authUseCase.login ( email, rawPassword, tokenGenerator, passwordMatches ) )
                .expectNextMatches ( auth -> auth.getToken ( ).equals ( "fake-token" ) &&
                        auth.getIdUser ( ).equals ( userId ) )
                .verifyComplete ( );

        verify ( userUseCase ).findByEmailAddress ( email );
    }

    @Test
    void shouldFailLoginIfUserNotFound() {
        String email = "notfound@example.com";
        String password = "123456";

        when ( userUseCase.findByEmailAddress ( email ) ).thenReturn ( Mono.empty ( ) );

        StepVerifier.create ( authUseCase.login ( email, password,
                        (u, r) -> "token", (p, s) -> true ) )
                .expectErrorMatches ( throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage ( ).equals ( "User not found" ) )
                .verify ( );

        verify ( userUseCase ).findByEmailAddress ( email );
    }

    @Test
    void shouldFailLoginIfPasswordIncorrect() {
        UUID userId = UUID.randomUUID ( );
        UUID rolId = UUID.randomUUID ( );
        String email = "axalpusa11125@gmail.com";
        String storedPassword = "hashedPassword";

        User user = new User ( );
        user.setIdUser ( userId );
        user.setIdRol ( rolId );
        user.setEmailAddress ( email );
        user.setPassword ( storedPassword );

        when ( userUseCase.findByEmailAddress ( email ) ).thenReturn ( Mono.just ( user ) );

        BiFunction < String, String, Boolean > passwordMatches = (raw, stored) -> false;

        StepVerifier.create ( authUseCase.login ( email, "wrongPassword", (u, r) -> "token", passwordMatches ) )
                .expectErrorMatches ( throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage ( ).equals ( "Invalid credentials" ) )
                .verify ( );

        verify ( userUseCase ).findByEmailAddress ( email );
    }


}
