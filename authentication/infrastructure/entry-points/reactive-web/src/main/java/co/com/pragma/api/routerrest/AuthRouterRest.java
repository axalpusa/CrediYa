package co.com.pragma.api.routerrest;

import co.com.pragma.api.handler.AuthHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class AuthRouterRest {

    @Bean
    public RouterFunction<ServerResponse> authRouterFunction(AuthHandler handler) {
        return route()
                .POST("/api/v1/login", handler::login)
                .build();
    }

}
