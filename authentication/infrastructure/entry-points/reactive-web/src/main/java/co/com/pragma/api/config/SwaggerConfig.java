package co.com.pragma.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI ( )
                .info ( new Info ( )
                        .title ( "Authentication Microservice" )
                        .version ( "v1" )
                        .description ( "Microservice for user authentication" ) );
    }
}
