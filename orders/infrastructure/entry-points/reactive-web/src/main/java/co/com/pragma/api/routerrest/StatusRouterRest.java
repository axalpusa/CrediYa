package co.com.pragma.api.routerrest;

import co.com.pragma.api.config.ApiPaths;
import co.com.pragma.api.handler.StatusHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class StatusRouterRest {

    @Bean
    public RouterFunction < ServerResponse > statusRoutes(StatusHandler handler) {
        return route ( )
                .POST ( ApiPaths.STATUS, handler::listenSaveStatus )
                .PUT ( ApiPaths.STATUS, handler::listenUpdateStatus )
                .DELETE ( ApiPaths.STATUSBYID, handler::listenDeleteStatus )
                .GET ( ApiPaths.STATUSALL, handler::listenGetAllStatus )
                .GET ( ApiPaths.STATUSBYID, handler::listenGetStatusById )
                .build ( );
    }
}
