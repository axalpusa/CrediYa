package co.com.pragma.api.config;

import co.com.pragma.api.dto.request.RolRequestDTO;
import co.com.pragma.api.dto.request.UserRequestDTO;
import co.com.pragma.api.enums.RolEnum;
import co.com.pragma.api.handler.UserHandler;
import co.com.pragma.api.jwt.JwtService;
import co.com.pragma.api.mapper.UserMapperDTO;
import co.com.pragma.api.routerrest.UserRouterRest;
import co.com.pragma.transaction.TransactionalAdapter;
import co.com.pragma.usecase.user.UserUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {UserRouterRest.class, UserHandler.class})
@WebFluxTest
@Import({
        UserRouterRest.class,
        UserHandler.class,
        CorsConfig.class,
        TestSecurityConfig.class,
        SecurityHeadersConfig.class,
        ConfigTest.MockBeans.class
})
class ConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TransactionalAdapter transactionalAdapter;

    @TestConfiguration
    static class MockBeans {
        @Bean
        UserUseCase saveUserUseCase() {
            return mock ( UserUseCase.class );
        }

        @Bean
        UserMapperDTO userDTOMapper() {
            return mock ( UserMapperDTO.class );
        }

        @Bean
        JwtService jwtService() {
            return mock ( JwtService.class );
        }

        @Bean
        PasswordEncoder passwordEncoder() {
            return mock ( PasswordEncoder.class );
        }

        @Bean
        TransactionalAdapter transactionalAdapter() {
            return mock(TransactionalAdapter.class);
        }
    }

    private UserRequestDTO buildUserRequest(UUID idRol) {
        UserRequestDTO req = new UserRequestDTO ( );
        req.setFirstName ( "axel" );
        req.setLastName ( "Puertas" );
        req.setAddress ( "Av santa rosa" );
        req.setEmailAddress ( "axalpusa11125@gmail.com" );
        req.setBirthDate ( LocalDate.parse ( "01-05-1994", DateTimeFormatter.ofPattern ( "dd-MM-yyyy" ) ) );
        req.setDocumentId ( "48594859" );
        req.setPhoneNumber ( "973157252" );
        req.setBaseSalary ( new BigDecimal ( "700000" ) );
        req.setPassword ( "$2a$10$mfILaHia4jqInB2mUQ2Vt.0PJxjJoXODUnkzchdHH6hxzPoF6xSjO" );
        req.setIdRol ( idRol );
        return req;
    }

    private RolRequestDTO buildRolRequest() {
        RolRequestDTO req = new RolRequestDTO ( );
        req.setName ( "ADMIN" );
        req.setDescription ( "DES. ADMIN" );
        return req;
    }

    private void expectSecurityHeaders(WebTestClient.ResponseSpec response) {
        response
                .expectHeader ( ).valueEquals ( "Content-Security-Policy",
                        "default-src 'self'; frame-ancestors 'self'; form-action 'self'" )
                .expectHeader ( ).value ( "Strict-Transport-Security",
                        v -> assertTrue ( v.startsWith ( "max-age=31536000" ) ) )
                .expectHeader ( ).valueEquals ( "X-Content-Type-Options", "nosniff" )
                .expectHeader ( ).value ( "Cache-Control",
                        v -> assertTrue ( v.contains ( "no-store" ) ) )
                .expectHeader ( ).valueEquals ( "Pragma", "no-cache" )
                .expectHeader ( ).valueEquals ( "Referrer-Policy", "strict-origin-when-cross-origin" )
                .expectHeader ( ).valueEquals ( "X-Frame-Options", "DENY" );
    }

    @Test
    void userSecurityHeadersShouldBePresent() {
        UUID idRolUser = RolEnum.ADMIN.getId();
        when(transactionalAdapter.executeInTransaction(any( Mono.class)))
                .thenAnswer(invocation -> invocation.<Mono<?>>getArgument(0));
        expectSecurityHeaders(
                webTestClient.post()
                        .uri(ApiPaths.USERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(buildUserRequest(idRolUser))
                        .exchange()
                        .expectStatus().isOk()
        );
    }

   /* @Test
    void rolSecurityHeadersShouldBePresent1() {
        UUID idRolUser = UUID.fromString ( "a71e243b-e901-4e6e-b521-85ff39ac2f3e" );
        WebTestClient.ResponseSpec response = webTestClient.post ( )
                .uri ( ApiPaths.USERS )
                .contentType ( MediaType.APPLICATION_JSON )
                .bodyValue ( buildUserRequest ( idRolUser ) )
                .exchange ( )
                .expectStatus ( ).isOk ( );
        expectSecurityHeaders ( response );
    }*/

}