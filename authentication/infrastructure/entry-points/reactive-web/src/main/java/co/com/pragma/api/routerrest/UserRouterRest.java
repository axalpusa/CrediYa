package co.com.pragma.api.routerrest;

import co.com.pragma.api.handler.UserHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class UserRouterRest {

    @Bean
    public RouterFunction<ServerResponse> userRouterFunction(UserHandler handler) {
        return route()
                .POST("/api/v1/users", handler::listenSaveUser)
                .PUT("/api/v1/users", handler::listenUpdateUser)
                .DELETE("/api/v1/users/{idUser}", handler::listenDeleteUser)
                .GET("/api/v1/users/all", handler::listenGetAllUsers)
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> undocumentedUserRouterFunction(UserHandler handler) {
        return RouterFunctions
                .route()
                .GET("/api/v1/users/byId/{idUser}", handler::listenGetUserById)
                .build();
    }
}
