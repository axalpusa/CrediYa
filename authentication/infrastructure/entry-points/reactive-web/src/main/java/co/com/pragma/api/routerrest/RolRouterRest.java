package co.com.pragma.api.routerrest;

import co.com.pragma.api.config.ApiPaths;
import co.com.pragma.api.handler.RolHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class RolRouterRest {

    @Bean
    public RouterFunction < ServerResponse > rolRouterFunction(RolHandler handler) {
        return route ( )
                .POST ( ApiPaths.ROL, handler::listenSaveRol )
                .PUT ( ApiPaths.ROL, handler::listenUpdateRol )
                .DELETE ( ApiPaths.ROLBYID, handler::listenDeleteRol )
                .GET ( ApiPaths.ROLLALL, handler::listenGetAllRols )
                .GET ( ApiPaths.ROLBYID, handler::listenGetRolById )
                .build ( );
    }
}
