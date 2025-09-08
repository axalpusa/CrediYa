package co.com.pragma.api;

import co.com.pragma.api.jwt.JwtAuthenticationFilter;
import co.com.pragma.api.jwt.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.*;

public class JwtAuthenticationFilterTest {
    private JwtService jwtService;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        jwtService = Mockito.mock(JwtService.class);
        filter = new JwtAuthenticationFilter(jwtService);
    }

    @Test
    void shouldPassRequestWithoutAuthorizationHeader() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/users")
        );

        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain).filter(exchange);
        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldAuthenticateRequestWithValidToken() {
        String token = "valid-token";
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        );

        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.deferContextual(ctx -> Mono.empty()));

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user-id");
        when(claims.get("idRol", String.class)).thenReturn("ADMIN");

        when(jwtService.extractAllClaims(token)).thenReturn(claims);

        StepVerifier.create(filter.filter(exchange, chain)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                                new UsernamePasswordAuthenticationToken("user-id", token, List.of())
                        )))
                .verifyComplete();

        verify(chain).filter(exchange);
        verify(jwtService).extractAllClaims(token);
    }


    @Test
    void shouldReturnUnauthorizedForInvalidToken() {
        String token = "invalid-token";
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        );

        WebFilterChain chain = mock(WebFilterChain.class);

        when(jwtService.extractAllClaims(token)).thenThrow(new RuntimeException("Invalid token"));

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assert exchange.getResponse().getStatusCode() == HttpStatus.UNAUTHORIZED;
    }
}
