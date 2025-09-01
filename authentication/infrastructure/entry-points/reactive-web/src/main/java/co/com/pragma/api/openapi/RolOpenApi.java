package co.com.pragma.api.openapi;

import co.com.pragma.api.dto.request.RolRequestDTO;
import co.com.pragma.api.dto.request.UserRequestDTO;
import co.com.pragma.api.handler.UserHandler;
import io.swagger.v3.oas.annotations.Operation;
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
public class RolOpenApi {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/rol",
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.POST,
                    beanClass = UserHandler.class,
                    beanMethod = "listenSaveRol",
                    operation = @Operation(
                            operationId = "saveUser",
                            summary = "Create a user",
                            tags = {"Users"},
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = RolRequestDTO.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Successful"),
                                    @ApiResponse(responseCode = "400", description = "Request invalid")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/rol/all",
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.GET,
                    beanClass = UserHandler.class,
                    beanMethod = "listenGetAllRols",
                    operation = @Operation(
                            operationId = "getAllRol",
                            summary = "Listar todos los roles",
                            tags = {"Rol"}
                    )
            )
    })
    public RouterFunction<ServerResponse> rolRoutesDoc() {
        return RouterFunctions.route(RequestPredicates.GET("/__dummy__"), req -> ServerResponse.ok().build());
    }
}
