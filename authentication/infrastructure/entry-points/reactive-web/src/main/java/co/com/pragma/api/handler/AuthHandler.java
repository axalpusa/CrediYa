package co.com.pragma.api.handler;

import co.com.pragma.api.dto.request.AuthRequestDTO;
import co.com.pragma.api.dto.response.AuthResponseDTO;
import co.com.pragma.api.jwt.JwtService;
import co.com.pragma.transaction.TransactionalAdapter;
import co.com.pragma.usecase.authentication.AuthUseCase;
import exceptions.UnauthorizedPragmaException;
import exceptions.ValidationPragmaException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static constants.Constants.REQUEST_EMPTY;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthHandler {

    private final AuthUseCase authUseCase;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final TransactionalAdapter transactionalAdapter;

    public Mono < ServerResponse > login(ServerRequest request) {
        return transactionalAdapter.executeInTransaction (
                request.bodyToMono ( AuthRequestDTO.class )
                        .switchIfEmpty ( Mono.error ( new RuntimeException ( REQUEST_EMPTY ) ) )
                        .flatMap ( dto -> authUseCase.login (
                                dto.getEmail ( ),
                                dto.getPassword ( ),
                                (userId, roleId) -> jwtService.generateToken ( userId, roleId ),
                                passwordEncoder::matches
                        ) )
                        .flatMap ( authResult -> ServerResponse.ok ( )
                                .contentType ( MediaType.APPLICATION_JSON )
                                .bodyValue ( authResult ) )
        );
    }

    public Mono < ServerResponse > validateToken(ServerRequest request) {
        String authHeader = request.headers ( ).firstHeader ( "Authorization" );

        if ( authHeader == null || !authHeader.startsWith ( "Bearer " ) ) {
            throw new ValidationPragmaException ( List.of ( "Missing or invalid Authorization header" ) );
        }

        String token = authHeader.substring ( 7 );

        if ( !jwtService.isTokenValid ( token ) ) {
            throw new UnauthorizedPragmaException ( "Invalid or expired token" );
        }

        UUID idUser = jwtService.extractUserId ( token );
        UUID idRol = jwtService.extractRoleId ( token );

        AuthResponseDTO response = AuthResponseDTO.builder ( )
                .idUser ( idUser )
                .idRol ( idRol )
                .token ( token )
                .build ( );

        return ServerResponse.ok ( )
                .contentType ( MediaType.APPLICATION_JSON )
                .bodyValue ( response );
    }
}
