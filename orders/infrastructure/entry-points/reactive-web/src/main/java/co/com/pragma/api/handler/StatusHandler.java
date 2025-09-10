package co.com.pragma.api.handler;

import co.com.pragma.api.config.ApiPaths;
import co.com.pragma.api.dto.request.StatusRequestDTO;
import co.com.pragma.api.dto.response.StatusResponseDTO;
import co.com.pragma.api.mapper.StatusMapperDTO;
import co.com.pragma.model.order.Order;
import co.com.pragma.model.status.Status;
import co.com.pragma.transaction.TransactionalAdapter;
import co.com.pragma.usecase.status.StatusUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import exceptions.NotFoundException;
import exceptions.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StatusHandler {

    private final StatusUseCase statusUseCase;
    private final ObjectMapper objectMapper;
    private final StatusMapperDTO statusMapper;
    private final TransactionalAdapter transactionalAdapter;

    public Mono < ServerResponse > listenSaveStatus(ServerRequest request) {
        return transactionalAdapter.executeInTransaction (
                request.bodyToMono ( StatusRequestDTO.class )
                        .switchIfEmpty ( Mono.error ( new ValidationException (
                                List.of ( "Request body cannot be empty" )
                        ) ) )
                        .flatMap ( dto -> Mono.justOrEmpty ( statusMapper.toModel ( dto ) ) )
                        .flatMap ( statusUseCase::saveStatus )
                        .flatMap ( status -> ServerResponse
                                .created ( URI.create ( ApiPaths.STATUS + status.getIdStatus ( ) ) )
                                .contentType ( MediaType.APPLICATION_JSON )
                                .bodyValue ( status ) )
        );
    }

    public Mono < ServerResponse > listenUpdateStatus(ServerRequest request) {
        return request.bodyToMono ( StatusResponseDTO.class )
                .flatMap ( dto -> statusUseCase.getStatusById ( dto.getIdStatus ( ) )
                        .switchIfEmpty ( Mono.error ( new NotFoundException ( "Status not found" ) ) )
                        .map ( existing -> {
                            Status partial = objectMapper.convertValue ( dto, Status.class );
                            if ( partial == null ) partial = new Status ( );
                            existing.merge ( partial );
                            return existing;
                        } )
                )
                .flatMap ( statusUseCase::updateStatus )
                .flatMap ( savedStatus -> ServerResponse.ok ( )
                        .contentType ( MediaType.APPLICATION_JSON )
                        .bodyValue ( savedStatus ) );
    }

    public Mono < ServerResponse > listenGetAllStatus(ServerRequest request) {
        return ServerResponse.ok ( )
                .contentType ( MediaType.TEXT_EVENT_STREAM )
                .body ( statusUseCase.getAlStatus ( ), StatusResponseDTO.class );
    }

    public Mono < ServerResponse > listenGetStatusById(ServerRequest request) {
        return Mono.fromCallable ( () -> request.pathVariable ( "idStatus" ) )
                .map ( String::trim )
                .filter ( item -> !item.isBlank ( ) )
                .map ( UUID::fromString )
                .flatMap ( statusUseCase::getStatusById )
                .flatMap ( status -> ServerResponse.ok ( )
                        .contentType ( MediaType.APPLICATION_JSON )
                        .bodyValue ( status ) )
                .switchIfEmpty ( ServerResponse.notFound ( ).build ( ) );
    }

    public Mono < ServerResponse > listenDeleteStatus(ServerRequest request) {

        return Mono.fromCallable ( () -> request.pathVariable ( "idStatus" ) )
                .map ( String::trim )
                .filter ( item -> !item.isBlank ( ) )
                .map ( UUID::fromString )
                .flatMap ( id -> statusUseCase.deleteStatusById ( id )
                        .then ( ServerResponse.noContent ( ).build ( ) )
                )
                .switchIfEmpty ( ServerResponse.notFound ( ).build ( ) );
    }

}
