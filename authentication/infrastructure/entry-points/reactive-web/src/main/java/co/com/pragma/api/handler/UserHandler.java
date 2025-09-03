package co.com.pragma.api.handler;

import co.com.pragma.api.config.ApiPaths;
import co.com.pragma.api.dto.request.UserRequestDTO;
import co.com.pragma.api.dto.response.UserReportResponseDTO;
import co.com.pragma.api.dto.response.UserResponseDTO;
import co.com.pragma.api.mapper.UserMapperDTO;
import co.com.pragma.model.user.User;
import co.com.pragma.transaction.TransactionalAdapter;
import co.com.pragma.usecase.user.UserUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import exceptions.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserHandler {

    private final UserUseCase userUseCase;
    private final ObjectMapper objectMapper;
    private final UserMapperDTO userMapperDTO;
    private final PasswordEncoder passwordEncoder;
    private final TransactionalAdapter transactionalAdapter;

    public Mono < ServerResponse > listenSaveUser(ServerRequest request) {
        return transactionalAdapter.executeInTransaction (
                request.bodyToMono ( UserRequestDTO.class )
                        .switchIfEmpty ( Mono.error ( new ValidationException (
                                List.of ( "Request body cannot be empty" )
                        ) ) )
                        // .map(userMapperDTO::toModel)
                        .flatMap ( dto -> Mono.justOrEmpty ( userMapperDTO.toModel ( dto ) ) )
                        .map ( user -> {
                            user.setPassword ( passwordEncoder.encode ( user.getPassword ( ) ) );
                            return user;
                        } )
                        .flatMap ( userUseCase::saveUser )
                        .flatMap ( user -> ServerResponse
                                .created ( URI.create ( ApiPaths.USERS + user.getIdUser ( ) ) )
                                .contentType ( MediaType.APPLICATION_JSON )
                                .bodyValue ( user ) )
                        .onErrorResume ( ValidationException.class, ex ->
                                ServerResponse.badRequest ( )
                                        .contentType ( MediaType.APPLICATION_JSON )
                                        .bodyValue ( Map.of ( "errors", ex.getErrors ( ) ) )
                        )
                        .onErrorResume ( e ->
                                ServerResponse.status ( HttpStatus.INTERNAL_SERVER_ERROR )
                                        .contentType ( MediaType.APPLICATION_JSON )
                                        .bodyValue ( Map.of (
                                                "message", "Unexpected error occurred",
                                                "details", e.getMessage ( )
                                        ) ) )
        );
    }


    public Mono < ServerResponse > listenUpdateUser(ServerRequest serverRequest) {
        return serverRequest.bodyToMono ( UserResponseDTO.class )
                .map ( user -> objectMapper.convertValue ( user, User.class ) )
                .flatMap ( userUseCase::updateUser )
                .flatMap ( savedUser -> ServerResponse.ok ( )
                        .contentType ( MediaType.APPLICATION_JSON )
                        .bodyValue ( savedUser ) );
    }

    public Mono < ServerResponse > listenGetAllUsers(ServerRequest request) {
        return ServerResponse.ok ( )
                .contentType ( MediaType.TEXT_EVENT_STREAM )
                .body ( userUseCase.getAllUsers ( ), UserResponseDTO.class );
    }

    public Mono < ServerResponse > listenGetUserById(ServerRequest serverRequest) {
        return Mono.fromCallable ( () -> serverRequest.pathVariable ( "idUser" ) )
                .map ( String::trim )
                .filter ( item -> !item.isBlank ( ) )
                .map ( UUID::fromString )
                .flatMap ( userUseCase::getUserById )
                .flatMap ( user -> ServerResponse.ok ( )
                        .contentType ( MediaType.APPLICATION_JSON )
                        .bodyValue ( user ) )
                .switchIfEmpty ( ServerResponse.notFound ( ).build ( ) );
    }

    public Mono < ServerResponse > listenDeleteUser(ServerRequest serverRequest) {

        return Mono.fromCallable ( () -> serverRequest.pathVariable ( "idUser" ) )
                .map ( String::trim )
                .filter ( item -> !item.isBlank ( ) )
                .map ( UUID::fromString )
                .flatMap ( id -> userUseCase.deleteUserById ( id )
                        .then ( ServerResponse.noContent ( ).build ( ) )
                )
                .switchIfEmpty ( ServerResponse.notFound ( ).build ( ) );
    }


    public Mono < ServerResponse > listenFindByEmailAddress(ServerRequest serverRequest) {
        return Mono.fromCallable ( () -> serverRequest.pathVariable ( "email" ) )
                .map ( email -> {
                    String decoded = URLDecoder.decode ( email, StandardCharsets.UTF_8 );
                    return decoded;
                } )
                .map ( String::trim )
                .filter ( email -> !email.isBlank ( ) )
                .flatMap ( userUseCase::findByEmailAddress )
                .map ( user -> {
                    UserReportResponseDTO dto = new UserReportResponseDTO ( );
                    dto.setEmailAddress ( user.getEmailAddress ( ) );
                    dto.setFirstName ( user.getFirstName ( ) );
                    dto.setLastName ( user.getLastName ( ) );
                    dto.setBaseSalary ( user.getBaseSalary ( ) );
                    return dto;
                } ).flatMap ( dto -> ServerResponse.ok ( )
                        .contentType ( MediaType.APPLICATION_JSON )
                        .bodyValue ( dto ) )
                .switchIfEmpty ( ServerResponse.notFound ( ).build ( ) );
    }
}

