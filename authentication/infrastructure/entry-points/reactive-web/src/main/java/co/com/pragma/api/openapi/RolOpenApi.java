package co.com.pragma.api.openapi;

import co.com.pragma.api.config.ApiPaths;
import co.com.pragma.api.dto.request.RolRequestDTO;
import co.com.pragma.api.handler.RolHandler;
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

import static constants.Constants.REQUEST_INVALID;
import static constants.Constants.ROL_FOUND;
import static constants.Constants.ROL_NOT_FOUND;
import static constants.Constants.SUCCESSFUL;

@Configuration
public class RolOpenApi {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = ApiPaths.ROL,
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.POST,
                    beanClass = RolHandler.class,
                    beanMethod = "listenSaveRol",
                    operation = @Operation(
                            operationId = "saveRol",
                            summary = "Create a rol",
                            tags = {"Rol"},
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = RolRequestDTO.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = SUCCESSFUL),
                                    @ApiResponse(responseCode = "400", description = REQUEST_INVALID)
                            }
                    )
            ),
            @RouterOperation(
                    path = ApiPaths.ROLLALL,
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.GET,
                    beanClass = RolHandler.class,
                    beanMethod = "listenGetAllRols",
                    operation = @Operation(
                            operationId = "getAllRol",
                            summary = "Listar todos los roles",
                            tags = {"Rol"}
                    )
            ),
            @RouterOperation(
                    path = ApiPaths.ROLBYID,
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.GET,
                    beanClass = RolHandler.class,
                    beanMethod = "listenGetRolById",
                    operation = @Operation(
                            operationId = "getRolById",
                            summary = "Get rol by ID",
                            tags = {"Rol"},
                            parameters = {
                                    @Parameter(
                                            name = "idRol",
                                            description = "ID rol (UUID)",
                                            required = true,
                                            in = ParameterIn.PATH,
                                            schema = @Schema(type = "string", format = "uuid")
                                    )
                            },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = ROL_FOUND),
                                    @ApiResponse(responseCode = "404", description = ROL_NOT_FOUND)
                            }
                    )
            )
    })
    public RouterFunction < ServerResponse > rolRoutesDoc() {
        return RouterFunctions.route (
                RequestPredicates.GET ( "/__dummy__" ),
                req -> ServerResponse.ok ( ).build ( )
        );
    }
}
