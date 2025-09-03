package co.com.pragma.api.routerrest;

import co.com.pragma.api.config.ApiPaths;
import co.com.pragma.api.handler.UserHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class UserRouterRest {

    @Bean
    public RouterFunction < ServerResponse > userRouterFunction(UserHandler handler) {
        return route ( )
                .POST ( ApiPaths.USERS, handler::listenSaveUser )
                .PUT ( ApiPaths.USERS, handler::listenUpdateUser )
                .DELETE ( ApiPaths.USERSBYID, handler::listenDeleteUser )
                .GET ( ApiPaths.USERSALL, handler::listenGetAllUsers )
                .GET ( ApiPaths.USERSBYID, handler::listenGetUserById )
                .GET ( ApiPaths.USERSBYEMAIL, handler::listenFindByEmailAddress )
                .build ( );
    }

}
