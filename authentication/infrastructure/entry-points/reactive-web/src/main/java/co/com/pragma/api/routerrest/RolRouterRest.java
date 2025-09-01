package co.com.pragma.api.routerrest;

import co.com.pragma.api.handler.RolHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class RolRouterRest {

    @Bean
    public RouterFunction<ServerResponse> rolRouterFunction(RolHandler handler) {
        return route()
                .POST("/api/v1/rol", handler::listenSaveRol)
                .PUT("/api/v1/rol", handler::listenUpdateRol)
                .DELETE("/api/v1/rol/{idRol}", handler::listenDeleteRol)
                .GET("/api/v1/rol/all", handler::listenGetAllRols)
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> undocumentedRolRouterFunction(RolHandler handler) {
        return RouterFunctions
                .route()
                .GET("/api/v1/rol/byId/{idRol}", handler::listenGetRolById)
                .build();
    }
}
