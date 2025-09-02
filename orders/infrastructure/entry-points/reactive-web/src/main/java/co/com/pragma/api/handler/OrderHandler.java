package co.com.pragma.api.handler;

import co.com.pragma.api.config.ApiPaths;
import co.com.pragma.api.dto.request.OrderRequestDTO;
import co.com.pragma.api.dto.response.AuthResponseDTO;
import co.com.pragma.api.dto.response.OrderResponseDTO;
import co.com.pragma.api.dto.response.ReportResponseDTO;
import co.com.pragma.api.enums.RolEnum;
import co.com.pragma.api.enums.StatusEnum;
import co.com.pragma.api.mapper.OrderMapperDTO;
import co.com.pragma.api.services.AuthServiceClient;
import co.com.pragma.model.order.Order;
import co.com.pragma.usecase.order.OrderUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import exceptions.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderHandler {

    private final OrderUseCase orderUseCase;
    private final ObjectMapper objectMapper;
    private final OrderMapperDTO orderMapper;
    private final AuthServiceClient authServiceClient;

    public Mono < ServerResponse > listenSaveOrder(ServerRequest request) {
        return validateUserToken(request,RolEnum.CLIENT.getId ( ) )
                .flatMap(authUser ->
                        request.bodyToMono(OrderRequestDTO.class)
                                .switchIfEmpty(Mono.error(new ValidationException(
                                        List.of("Request body cannot be empty")
                                )))
                                .flatMap(dto -> {
                                    Order order = orderMapper.toModel(dto);
                                    order.setIdStatus(StatusEnum.REVISION.getId());
                                    return orderUseCase.saveOrder(order);
                                })
                                .flatMap(savedOrder ->
                                        ServerResponse.created(URI.create(ApiPaths.ORDER + savedOrder.getIdOrder()))
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .bodyValue(savedOrder)
                                )
                )
                .onErrorResume ( ValidationException.class, ex ->
                        ServerResponse.badRequest ( )
                                .bodyValue ( Map.of ( "errors", ex.getErrors ( ) ) )
                )
                .onErrorResume( WebClientResponseException.Unauthorized.class, ex ->
                        ServerResponse.status(HttpStatus.UNAUTHORIZED)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(Map.of("errors", "Token inválido o expirado"))
                )
                .onErrorResume(WebClientResponseException.Forbidden.class, ex ->
                        ServerResponse.status(HttpStatus.FORBIDDEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(Map.of("errors", "Acceso denegado"))
                )
                .onErrorResume(ValidationException.class, ex ->
                        ServerResponse.badRequest()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(Map.of("errors", ex.getErrors()))
                )
                .onErrorResume(RuntimeException.class, ex ->
                        ServerResponse.status(HttpStatus.UNAUTHORIZED)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(Map.of("errors", ex.getMessage()))
                )
                .onErrorResume(Exception.class, ex ->
                        ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(Map.of("message", "Unexpected error", "details", ex.getMessage()))
                );
    }

    public Mono < ServerResponse > listenReportOrder(ServerRequest request) {
        String filterEmail = request.queryParam("email").orElse("");
        int page = request.queryParam("page").map(Integer::parseInt).orElse(0);
        int size = request.queryParam("size").map(Integer::parseInt).orElse(10);

        return validateUserToken(request,RolEnum.ASSESSOR.getId ( ) )
                .flatMap(authUser ->
                        ServerResponse.ok ()
                                .contentType ( MediaType.APPLICATION_JSON )
                                .body ( orderUseCase.findPendingOrders ( filterEmail,page,size )
                                        .map ( dto -> new ReportResponseDTO (
                                                dto.getAmount (),
                                                dto.getTermMonths (),
                                                dto.getEmail (),
                                                dto.getTypeLoan (),
                                                dto.getInterestRate (),
                                                dto.
                                        ) ))

                )
                .onErrorResume ( ValidationException.class, ex ->
                        ServerResponse.badRequest ( )
                                .bodyValue ( Map.of ( "errors", ex.getErrors ( ) ) )
                )
                .onErrorResume( WebClientResponseException.Unauthorized.class, ex ->
                        ServerResponse.status(HttpStatus.UNAUTHORIZED)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(Map.of("errors", "Token inválido o expirado"))
                )
                .onErrorResume(WebClientResponseException.Forbidden.class, ex ->
                        ServerResponse.status(HttpStatus.FORBIDDEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(Map.of("errors", "Acceso denegado"))
                )
                .onErrorResume(ValidationException.class, ex ->
                        ServerResponse.badRequest()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(Map.of("errors", ex.getErrors()))
                )
                .onErrorResume(RuntimeException.class, ex ->
                        ServerResponse.status(HttpStatus.UNAUTHORIZED)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(Map.of("errors", ex.getMessage()))
                )
                .onErrorResume(Exception.class, ex ->
                        ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(Map.of("message", "Unexpected error", "details", ex.getMessage()))
                );
    }
    private Mono < AuthResponseDTO > validateUserToken(ServerRequest request,UUID idRol) {
        String authHeader = request.headers ( ).firstHeader ( "Authorization" );
        if ( authHeader == null || !authHeader.startsWith ( "Bearer " ) ) {

            return Mono.error ( new RuntimeException ( "Authorization header missing or invalid" ) );
        }
        String token = authHeader.substring ( 7 );

        return authServiceClient.validateToken ( token )
                .flatMap ( user -> {
                    boolean allowed = user.getIdRol ( ).equals ( idRol );
                    if ( !allowed ) {
                        return Mono.error ( new RuntimeException ( "User is not allowed to create orders" ) );
                    }
                    return Mono.just ( user );
                } );
    }

    public Mono < ServerResponse > listenUpdateOrder(ServerRequest request) {
        return request.bodyToMono ( OrderResponseDTO.class )
                .map ( order -> objectMapper.convertValue ( order, Order.class ) )
                .flatMap ( orderUseCase::updateOrder )
                .flatMap ( savedOrder -> ServerResponse.ok ( )
                        .contentType ( MediaType.APPLICATION_JSON )
                        .bodyValue ( savedOrder ) );
    }

    public Mono < ServerResponse > listenGetAllOrders(ServerRequest request) {
        return ServerResponse.ok ( )
                .contentType ( MediaType.TEXT_EVENT_STREAM )
                .body ( orderUseCase.getAllOrders ( ), OrderResponseDTO.class );
    }

    public Mono < ServerResponse > listenGetOrderById(ServerRequest request) {
        return Mono.fromCallable ( () -> request.pathVariable ( "idOrder" ) )
                .map ( String::trim )
                .filter ( item -> !item.isBlank ( ) )
                .map ( UUID::fromString )
                .flatMap ( orderUseCase::getOrderById )
                .flatMap ( order -> ServerResponse.ok ( )
                        .contentType ( MediaType.APPLICATION_JSON )
                        .bodyValue ( order ) )
                .switchIfEmpty ( ServerResponse.notFound ( ).build ( ) );
    }

    public Mono < ServerResponse > listenDeleteOrder(ServerRequest request) {

        return Mono.fromCallable ( () -> request.pathVariable ( "idOrder" ) )
                .map ( String::trim )
                .filter ( item -> !item.isBlank ( ) )
                .map ( UUID::fromString )
                .flatMap ( id -> orderUseCase.deleteOrderById ( id )
                        .then ( ServerResponse.noContent ( ).build ( ) )
                )
                .switchIfEmpty ( ServerResponse.notFound ( ).build ( ) );
    }

}
