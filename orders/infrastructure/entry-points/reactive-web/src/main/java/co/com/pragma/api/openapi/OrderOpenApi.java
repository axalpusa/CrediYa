package co.com.pragma.api.openapi;

import co.com.pragma.api.config.ApiPaths;
import co.com.pragma.api.dto.request.OrderRequestDTO;
import co.com.pragma.api.handler.OrderHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class OrderOpenApi {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = ApiPaths.ORDER,
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.POST,
                    beanClass = OrderHandler.class,
                    beanMethod = "listenSaveOrder",
                    operation = @Operation(
                            operationId = "saveOrder",
                            summary = "Create a order",
                            tags = {"Order"},
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = OrderRequestDTO.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Successful"),
                                    @ApiResponse(responseCode = "400", description = "Request invalid")
                            }
                    )
            ),
            @RouterOperation(
                    path = ApiPaths.ORDERALL,
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.GET,
                    beanClass = OrderHandler.class,
                    beanMethod = "listenGetAllOrders",
                    operation = @Operation(
                            operationId = "getAllOrders",
                            summary = "List all order",
                            tags = {"Order"}
                    )
            ),
            @RouterOperation(
                    path = ApiPaths.ORDERBYID,
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.GET,
                    beanClass = OrderHandler.class,
                    beanMethod = "listenGetOrderById",
                    operation = @Operation(
                            operationId = "getOrderById",
                            summary = "Get order by ID",
                            tags = {"Order"},
                            parameters = {
                                    @Parameter(
                                            name = "idOrder",
                                            description = "ID order (UUID)",
                                            required = true,
                                            in = ParameterIn.PATH,
                                            schema = @Schema(type = "string", format = "uuid")
                                    )
                            },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Order found"),
                                    @ApiResponse(responseCode = "404", description = "Order not found")
                            }
                    )
            )
    })
    public RouterFunction < ServerResponse > orderRoutesDoc() {
        return RouterFunctions.route (
                RequestPredicates.GET ( "/__dummy__" ),
                req -> ServerResponse.ok ( ).build ( )
        );
    }
}
