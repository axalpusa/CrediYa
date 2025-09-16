package co.com.pragma.api.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityHeadersConfigTest {

    private SecurityHeadersConfig securityHeadersConfig;

    @BeforeEach
    void setUp() {
        securityHeadersConfig = new SecurityHeadersConfig ( );
    }

    @Test
    void shouldAddSecurityHeaders() {
        MockServerWebExchange exchange = MockServerWebExchange.from (
                org.springframework.mock.http.server.reactive.MockServerHttpRequest.get ( "/test" ).build ( )
        );
        WebFilterChain filterChain = webExchange -> Mono.empty ( );

        securityHeadersConfig.filter ( exchange, filterChain ).block ( );

        MockServerHttpResponse response = exchange.getResponse ( );
        var headers = response.getHeaders ( );

        assertThat ( headers.getFirst ( "Content-Security-Policy" ) )
                .isEqualTo ( "default-src 'self'; frame-ancestors 'self'; form-action 'self'" );
        assertThat ( headers.getFirst ( "Strict-Transport-Security" ) )
                .isEqualTo ( "max-age=31536000;" );
        assertThat ( headers.getFirst ( "X-Content-Type-Options" ) )
                .isEqualTo ( "nosniff" );
        assertThat ( headers.getFirst ( "Server" ) )
                .isEqualTo ( "" );
        assertThat ( headers.getFirst ( "Cache-Control" ) )
                .isEqualTo ( "no-store" );
        assertThat ( headers.getFirst ( "Pragma" ) )
                .isEqualTo ( "no-cache" );
        assertThat ( headers.getFirst ( "Referrer-Policy" ) )
                .isEqualTo ( "strict-origin-when-cross-origin" );
        assertThat ( headers.getFirst ( "X-Frame-Options" ) )
                .isEqualTo ( "DENY" );
    }
}
