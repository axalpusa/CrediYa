package co.com.pragma.api.routerrest;

import co.com.pragma.api.config.ApiPaths;
import co.com.pragma.api.handler.OrderHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class OrderRouterRest {

    @Bean
    public RouterFunction < ServerResponse > orderRoutes(OrderHandler handler) {
        return route ( )
                .POST ( ApiPaths.ORDER, handler::listenSaveOrder )
                .PUT ( ApiPaths.ORDER, handler::listenUpdateOrder )
                .DELETE ( ApiPaths.ORDERBYID, handler::listenDeleteOrder )
                .GET ( ApiPaths.ORDERALL, handler::listenGetAllOrders )
                .GET ( ApiPaths.ORDERBYID, handler::listenGetOrderById )
                .POST ( ApiPaths.REPORT, handler::listenReportOrder )
                .build ( );
    }
}
