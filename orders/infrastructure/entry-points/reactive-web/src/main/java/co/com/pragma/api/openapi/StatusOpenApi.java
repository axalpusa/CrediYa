package co.com.pragma.api.openapi;

import co.com.pragma.api.config.ApiPaths;
import co.com.pragma.api.dto.request.StatusRequestDTO;
import co.com.pragma.api.handler.StatusHandler;
import constants.Constants;
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
public class StatusOpenApi {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = ApiPaths.STATUS,
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.POST,
                    beanClass = StatusHandler.class,
                    beanMethod = "listenSaveStatus",
                    operation = @Operation(
                            operationId = "saveStatus",
                            summary = "Create a status",
                            tags = {"Status"},
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = StatusRequestDTO.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = Constants.SUCCESSFUL),
                                    @ApiResponse(responseCode = "400", description = Constants.REQUEST_INVALID)
                            }
                    )
            ),
            @RouterOperation(
                    path = ApiPaths.STATUSALL,
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.GET,
                    beanClass = StatusHandler.class,
                    beanMethod = "listenGetAllStatus",
                    operation = @Operation(
                            operationId = "getAllStatus",
                            summary = "List all status",
                            tags = {"Status"}
                    )
            ),
            @RouterOperation(
                    path = ApiPaths.STATUSBYID,
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.GET,
                    beanClass = StatusHandler.class,
                    beanMethod = "listenGetStatusById",
                    operation = @Operation(
                            operationId = "getStatusById",
                            summary = "Get status by ID",
                            tags = {"Status"},
                            parameters = {
                                    @Parameter(
                                            name = "idStatus",
                                            description = "ID status (UUID)",
                                            required = true,
                                            in = ParameterIn.PATH,
                                            schema = @Schema(type = "string", format = "uuid")
                                    )
                            },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = Constants.STATUS_FOUND),
                                    @ApiResponse(responseCode = "404", description = Constants.STATUS_NOT_FOUND)
                            }
                    )
            )
    })
    public RouterFunction < ServerResponse > statusRoutesDoc() {
        return RouterFunctions.route (
                RequestPredicates.GET ( "/__dummy__" ),
                req -> ServerResponse.ok ( ).build ( )
        );
    }
}
