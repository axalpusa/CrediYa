package co.com.pragma.api.handler;

import co.com.pragma.api.config.ApiPaths;
import co.com.pragma.api.dto.request.OrderRequestDTO;
import co.com.pragma.api.dto.response.AuthResponseDTO;
import co.com.pragma.api.dto.response.OrderResponseDTO;
import co.com.pragma.api.dto.response.ReportResponseDTO;
import co.com.pragma.api.dto.response.UserReportResponseDTO;
import co.com.pragma.api.mapper.OrderMapperDTO;
import co.com.pragma.api.services.AuthServiceClient;
import co.com.pragma.model.order.OrderPending;
import co.com.pragma.model.order.Order;
import co.com.pragma.transaction.TransactionalAdapter;
import co.com.pragma.usecase.order.OrderUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import enums.RolEnum;
import enums.StatusEnum;
import exceptions.NotFoundException;
import exceptions.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderHandler {

    private final OrderUseCase orderUseCase;
    private final ObjectMapper objectMapper;
    private final OrderMapperDTO orderMapper;
    private final AuthServiceClient authServiceClient;
    private final TransactionalAdapter transactionalAdapter;

    public Mono<ServerResponse> listenSaveOrder(ServerRequest request) {
        return transactionalAdapter.executeInTransaction(
                validateUserToken(request, RolEnum.CLIENT.getId())
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
        );
    }

    public Mono<ServerResponse> listenReportOrder(ServerRequest request) {
        UUID filterStatus = request.queryParam("status")
                .filter(s -> !s.isBlank())
                .map(UUID::fromString)
                .orElse(null);
        int page = request.queryParam("page")
                .map(Integer::parseInt)
                .orElse(0);
        int size = request.queryParam("size")
                .map(Integer::parseInt)
                .orElse(10);
        return validateUserToken(request, RolEnum.ASSESSOR.getId())
                .flatMap(authUser ->
                        orderUseCase.findPendingOrders(filterStatus, page, size)
                                .flatMap(order ->
                                        authServiceClient.getUserByEmailAddress(authUser.getToken(), order.getEmail())
                                                .onErrorResume(WebClientResponseException.NotFound.class, ex -> Mono.empty())
                                                .map(user -> mapToReportDTO(order, user))
                                )
                                .collectList()
                )
                .flatMap(list -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(list)
                );
    }


    private ReportResponseDTO mapToReportDTO(OrderPending order, UserReportResponseDTO user) {
        ReportResponseDTO report = new ReportResponseDTO();
        report.setStatusOrder(order.getStatusOrder());
        report.setAmount(order.getAmount());
        report.setEmail(user.getEmailAddress());
        report.setName(user.getFirstName() + " " + user.getLastName());
        report.setBaseSalary(user.getBaseSalary());
        report.setTypeLoan(order.getTypeLoan());
        report.setInterestRate(order.getInterestRate());
        report.setTermMonths(order.getTermMonths());
        report.setTotalMonthlyDebtApprovedRequests(order.getTotalMonthlyDebtApprovedRequests());
        return report;
    }

    private Mono<AuthResponseDTO> validateUserToken(ServerRequest request, UUID idRol) {
        String authHeader = request.headers().firstHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {

            return Mono.error(new RuntimeException("Authorization header missing or invalid"));
        }
        String token = authHeader.substring(7);

        return authServiceClient.validateToken(token)
                .flatMap(user -> {
                    boolean allowed = user.getIdRol().equals(idRol);
                    if (!allowed) {
                        return Mono.error(new RuntimeException("User is not alloweds"));
                    }
                    return Mono.just(user);
                });
    }

    public Mono<ServerResponse> listenUpdateOrder(ServerRequest request) {
        return transactionalAdapter.executeInTransaction(
                validateUserToken(request, RolEnum.ASSESSOR.getId())
                        .flatMap(authUser ->
                                request.bodyToMono(OrderResponseDTO.class)
                                        .flatMap(dto -> orderUseCase.getOrderById(dto.getIdOrder())
                                                .switchIfEmpty(Mono.error(new NotFoundException("Order not found")))
                                                .flatMap(existing -> {
                                                    Order partial = objectMapper.convertValue(dto, Order.class);
                                                    if (partial == null) partial = new Order();
                                                    existing.merge(partial);

                                                    UUID newStatus = existing.getIdStatus();
                                                    Mono<Order> updateMono = (StatusEnum.APPROVED.getId().equals(newStatus)
                                                            || StatusEnum.REJECTED.getId().equals(newStatus))
                                                            ? orderUseCase.updateOrderAndNotify(existing)
                                                            : orderUseCase.updateOrder(existing);

                                                    return updateMono; // Esto debe ser un Mono, no null
                                                })
                                        )
                                        .flatMap(savedOrder ->
                                                ServerResponse.ok()
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .bodyValue(savedOrder)
                                        )
                        )
        );
    }


    public Mono<ServerResponse> listenGetAllOrders(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(orderUseCase.getAllOrders(), OrderResponseDTO.class);
    }

    public Mono<ServerResponse> listenGetOrderById(ServerRequest request) {
        return Mono.fromCallable(() -> request.pathVariable("idOrder"))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .map(UUID::fromString)
                .flatMap(orderUseCase::getOrderById)
                .flatMap(order -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(order))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> listenDeleteOrder(ServerRequest request) {

        return Mono.fromCallable(() -> request.pathVariable("idOrder"))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .map(UUID::fromString)
                .flatMap(id -> orderUseCase.deleteOrderById(id)
                        .then(ServerResponse.noContent().build())
                )
                .switchIfEmpty(ServerResponse.notFound().build());
    }

}
