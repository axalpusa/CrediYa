package co.com.pragma.api.openapi;

import co.com.pragma.api.config.ApiPaths;
import co.com.pragma.api.dto.request.TypeLoanRequestDTO;
import co.com.pragma.api.handler.TypeLoanHandler;
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
public class TypeLoanOpenApi {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = ApiPaths.TYPELOAN,
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.POST,
                    beanClass = TypeLoanHandler.class,
                    beanMethod = "listenSaveTypeLoan",
                    operation = @Operation(
                            operationId = "savetypeLoan",
                            summary = "Create a type loan",
                            tags = {"Type loan"},
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = TypeLoanRequestDTO.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = Constants.SUCCESSFUL),
                                    @ApiResponse(responseCode = "400", description = Constants.REQUEST_INVALID)
                            }
                    )
            ),
            @RouterOperation(
                    path = ApiPaths.TYPELOANSALL,
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.GET,
                    beanClass = TypeLoanHandler.class,
                    beanMethod = "listenGetAllTypesLoan",
                    operation = @Operation(
                            operationId = "getAlltypesLoan",
                            summary = "List all types loan",
                            tags = {"Type loan"}
                    )
            ),
            @RouterOperation(
                    path = ApiPaths.TYPELOANBYID,
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.GET,
                    beanClass = TypeLoanHandler.class,
                    beanMethod = "listenGetTypeLoanById",
                    operation = @Operation(
                            operationId = "getTypeLoanById",
                            summary = "Get type loan by ID",
                            tags = {"Type loan"},
                            parameters = {
                                    @Parameter(
                                            name = "idTypeLoan",
                                            description = "ID type loan (UUID)",
                                            required = true,
                                            in = ParameterIn.PATH,
                                            schema = @Schema(type = "string", format = "uuid")
                                    )
                            },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = Constants.TYPE_LOAN_FOUND),
                                    @ApiResponse(responseCode = "404", description = Constants.TYPE_LOAN_NOT_FOUND)
                            }
                    )
            )
    })
    public RouterFunction < ServerResponse > typeLoanRoutesDoc() {
        return RouterFunctions.route (
                RequestPredicates.GET ( "/__dummy__" ),
                req -> ServerResponse.ok ( ).build ( )
        );
    }
}
