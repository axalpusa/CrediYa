package co.com.pragma.api.handler;

import co.com.pragma.api.config.ApiPaths;
import co.com.pragma.api.dto.request.OrderRequestDTO;
import co.com.pragma.api.dto.request.ReportRequestDTO;
import co.com.pragma.api.dto.response.AuthResponseDTO;
import co.com.pragma.api.dto.response.OrderResponseDTO;
import co.com.pragma.api.dto.response.ReportResponseDTO;
import co.com.pragma.api.dto.response.UserReportResponseDTO;
import co.com.pragma.api.enums.RolEnum;
import co.com.pragma.api.enums.StatusEnum;
import co.com.pragma.api.mapper.OrderMapperDTO;
import co.com.pragma.api.services.AuthServiceClient;
import co.com.pragma.model.dto.OrderPendingDTO;
import co.com.pragma.model.order.Order;
import co.com.pragma.transaction.TransactionalAdapter;
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
    private final TransactionalAdapter transactionalAdapter;

    public Mono < ServerResponse > listenSaveOrder(ServerRequest request) {
        return transactionalAdapter.executeInTransaction (
                validateUserToken ( request, RolEnum.CLIENT.getId ( ) )
                        .flatMap ( authUser ->
                                request.bodyToMono ( OrderRequestDTO.class )
                                        .switchIfEmpty ( Mono.error ( new ValidationException (
                                                List.of ( "Request body cannot be empty" )
                                        ) ) )
                                        .flatMap ( dto -> {
                                            Order order = orderMapper.toModel ( dto );
                                            order.setIdStatus ( StatusEnum.REVISION.getId ( ) );
                                            return orderUseCase.saveOrder ( order );
                                        } )
                                        .flatMap ( savedOrder ->
                                                ServerResponse.created ( URI.create ( ApiPaths.ORDER + savedOrder.getIdOrder ( ) ) )
                                                        .contentType ( MediaType.APPLICATION_JSON )
                                                        .bodyValue ( savedOrder )
                                        )
                        )
                        .onErrorResume ( this::handleError )
        );
    }

    public Mono < ServerResponse > listenReportOrder(ServerRequest request) {
        return request.bodyToMono ( ReportRequestDTO.class )
                .flatMap ( req ->
                        validateUserToken ( request, RolEnum.ASSESSOR.getId ( ) )
                                .flatMap ( authUser ->
                                        orderUseCase.findPendingOrders ( req.getStatus ( ), req.getEmail ( ), req.getPage ( ), req.getSize ( ) )
                                                .flatMap ( order ->
                                                        authServiceClient.getUserByEmailAddress ( authUser.getToken ( ), order.getEmail ( ) )
                                                                .onErrorResume ( WebClientResponseException.NotFound.class, ex -> Mono.empty ( ) )
                                                                .map ( user -> mapToReportDTO ( order, user ) )
                                                )
                                                .collectList ( )
                                )
                )
                .flatMap ( list -> ServerResponse.ok ( )
                        .contentType ( MediaType.APPLICATION_JSON )
                        .bodyValue ( list ) )
                .onErrorResume ( this::handleError );
    }

    private ReportResponseDTO mapToReportDTO(OrderPendingDTO order, UserReportResponseDTO user) {
        ReportResponseDTO report = new ReportResponseDTO ( );
        report.setStatusOrder ( order.getStatusOrder ( ) );
        report.setAmount ( order.getAmount ( ) );
        report.setEmail ( user.getEmailAddress ( ) );
        report.setName ( user.getFirstName ( ) + " " + user.getLastName ( ) );
        report.setBaseSalary ( user.getBaseSalary ( ) );
        report.setTypeLoan ( order.getTypeLoan ( ) );
        report.setInterestRate ( order.getInterestRate ( ) );
        report.setTermMonths ( order.getTermMonths ( ) );
        report.setTotalMonthlyDebtApprovedRequests ( order.getTotalMonthlyDebtApprovedRequests ( ) );
        return report;
    }

    private Mono < ServerResponse > handleError(Throwable ex) {
        if ( ex instanceof ValidationException ve ) {
            return ServerResponse.badRequest ( )
                    .contentType ( MediaType.APPLICATION_JSON )
                    .bodyValue ( Map.of ( "errors", ve.getErrors ( ) ) );
        } else if ( ex instanceof WebClientResponseException.Unauthorized ) {
            return ServerResponse.status ( HttpStatus.UNAUTHORIZED )
                    .contentType ( MediaType.APPLICATION_JSON )
                    .bodyValue ( Map.of ( "errors", "Token inválido o expirado" ) );
        } else if ( ex instanceof WebClientResponseException.Forbidden ) {
            return ServerResponse.status ( HttpStatus.FORBIDDEN )
                    .contentType ( MediaType.APPLICATION_JSON )
                    .bodyValue ( Map.of ( "errors", "Acceso denegado" ) );
        } else {
            return ServerResponse.status ( HttpStatus.INTERNAL_SERVER_ERROR )
                    .contentType ( MediaType.APPLICATION_JSON )
                    .bodyValue ( Map.of ( "errors", ex.getMessage ( ) ) );
        }
    }

    private Mono < AuthResponseDTO > validateUserToken(ServerRequest request, UUID idRol) {
        String authHeader = request.headers ( ).firstHeader ( "Authorization" );
        if ( authHeader == null || !authHeader.startsWith ( "Bearer " ) ) {

            return Mono.error ( new RuntimeException ( "Authorization header missing or invalid" ) );
        }
        String token = authHeader.substring ( 7 );

        return authServiceClient.validateToken ( token )
                .flatMap ( user -> {
                    boolean allowed = user.getIdRol ( ).equals ( idRol );
                    if ( !allowed ) {
                        return Mono.error ( new RuntimeException ( "User is not alloweds" ) );
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
