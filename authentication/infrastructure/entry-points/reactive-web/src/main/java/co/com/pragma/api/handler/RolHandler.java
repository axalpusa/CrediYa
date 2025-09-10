package co.com.pragma.api.handler;

import co.com.pragma.api.config.ApiPaths;
import co.com.pragma.api.dto.request.RolRequestDTO;
import co.com.pragma.api.dto.response.RolResponseDTO;
import co.com.pragma.api.mapper.RolMapperDTO;
import co.com.pragma.model.rol.Rol;
import co.com.pragma.transaction.TransactionalAdapter;
import co.com.pragma.usecase.rol.RolUseCase;
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
public class RolHandler {

    private final RolUseCase rolUseCase;
    private final ObjectMapper objectMapper;
    private final RolMapperDTO rolMapper;
    private final TransactionalAdapter transactionalAdapter;

    public Mono < ServerResponse > listenSaveRol(ServerRequest request) {
        return transactionalAdapter.executeInTransaction (
                request.bodyToMono ( RolRequestDTO.class )
                        .switchIfEmpty ( Mono.error ( new ValidationException (
                                List.of ( "Request body cannot be empty" )
                        ) ) )
                        .flatMap ( dto -> Mono.justOrEmpty ( rolMapper.toModel ( dto ) ) )
                        .flatMap ( rolUseCase::saveRol )
                        .flatMap ( savedRol -> ServerResponse
                                .created ( URI.create ( ApiPaths.ROL + savedRol.getIdRol ( ) ) )
                                .contentType ( MediaType.APPLICATION_JSON )
                                .bodyValue ( savedRol ) )
        );
    }

    public Mono<ServerResponse> listenUpdateRol(ServerRequest request) {
        return request.bodyToMono(RolResponseDTO.class)
                .flatMap(dto -> rolUseCase.getRolById (dto.getIdRol())
                        .switchIfEmpty(Mono.error(new NotFoundException("Rol not found")))
                        .map(existingRol -> {
                            Rol partial = objectMapper.convertValue(dto, Rol.class);
                            if (partial == null) partial = new Rol();
                            existingRol.merge(partial);
                            return existingRol;
                        })
                )
                .flatMap(rolUseCase::updateRol)
                .flatMap(savedRol -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(savedRol));
    }


    public Mono < ServerResponse > listenGetAllRols(ServerRequest request) {
        return ServerResponse.ok ( )
                .contentType ( MediaType.TEXT_EVENT_STREAM )
                .body ( rolUseCase.getAllRol ( ), RolResponseDTO.class );
    }

    public Mono < ServerResponse > listenGetRolById(ServerRequest request) {
        return Mono.fromCallable ( () -> request.pathVariable ( "idRol" ) )
                .map ( String::trim )
                .filter ( item -> !item.isBlank ( ) )
                .map ( UUID::fromString )
                .flatMap ( rolUseCase::getRolById )
                .flatMap ( rol -> ServerResponse.ok ( )
                        .contentType ( MediaType.APPLICATION_JSON )
                        .bodyValue ( rol ) )
                .switchIfEmpty ( ServerResponse.notFound ( ).build ( ) );
    }

    public Mono < ServerResponse > listenDeleteRol(ServerRequest request) {

        return Mono.fromCallable ( () -> request.pathVariable ( "idRol" ) )
                .map ( String::trim )
                .filter ( item -> !item.isBlank ( ) )
                .map ( UUID::fromString )
                .flatMap ( id -> rolUseCase.deleteRolById ( id )
                        .then ( ServerResponse.noContent ( ).build ( ) )
                )
                .switchIfEmpty ( ServerResponse.notFound ( ).build ( ) );
    }

}
