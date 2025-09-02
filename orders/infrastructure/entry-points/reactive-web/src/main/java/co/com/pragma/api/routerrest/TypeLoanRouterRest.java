package co.com.pragma.api.routerrest;

import co.com.pragma.api.config.ApiPaths;
import co.com.pragma.api.handler.TypeLoanHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class TypeLoanRouterRest {

    @Bean
    public RouterFunction < ServerResponse > typeLoanRoutes(TypeLoanHandler handler) {
        return route ( )
                .POST ( ApiPaths.TYPELOAN, handler::listenSaveTypeLoan )
                .PUT ( ApiPaths.TYPELOAN, handler::listenUpdateTypeLoan )
                .DELETE ( ApiPaths.TYPELOANBYID, handler::listenDeleteTypeLoan )
                .GET ( ApiPaths.TYPELOANSALL, handler::listenGetAllTypesLoan )
                .GET ( ApiPaths.TYPELOANBYID, handler::listenGetTypeLoanById )
                .build ( );
    }
}
