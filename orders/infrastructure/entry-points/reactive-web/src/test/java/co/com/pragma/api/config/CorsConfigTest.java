package co.com.pragma.api.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ContextConfiguration(classes = {CorsConfig.class})
class CorsConfigTest {

    @Test
    void shouldCreateCorsWebFilter() {
        CorsConfig corsConfig = new CorsConfig ( );
        CorsWebFilter filter = corsConfig.corsWebFilter ( "http://localhost:3000" );

        assertThat ( filter ).isNotNull ( );
    }
}