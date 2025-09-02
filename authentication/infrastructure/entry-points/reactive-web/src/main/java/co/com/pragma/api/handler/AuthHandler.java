package co.com.pragma.api.handler;

import co.com.pragma.api.dto.request.AuthRequestDTO;
import co.com.pragma.api.dto.response.AuthResponseDTO;
import co.com.pragma.api.jwt.JwtService;
import co.com.pragma.usecase.authentication.AuthUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthHandler {

    private final AuthUseCase authUseCase;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public Mono < ServerResponse > login(ServerRequest request) {
        return request.bodyToMono ( AuthRequestDTO.class )
                .switchIfEmpty ( Mono.error ( new RuntimeException ( "Request body cannot be empty" ) ) )
                .flatMap ( dto -> authUseCase.login (
                        dto.getEmail ( ),
                        dto.getPassword ( ),
                        (userId, roleId) -> jwtService.generateToken ( userId, roleId ),
                        passwordEncoder::matches
                ) )
                .flatMap ( authResult -> ServerResponse.ok ( )
                        .contentType ( MediaType.APPLICATION_JSON )
                        .bodyValue ( authResult ) )
                .onErrorResume ( e -> {
                    String message = e.getMessage ( ) != null ? e.getMessage ( ) : "Unexpected error";

                    if ( "Invalid credentials".equalsIgnoreCase ( message ) ) {
                        return ServerResponse.status ( HttpStatus.UNAUTHORIZED )
                                .contentType ( MediaType.APPLICATION_JSON )
                                .bodyValue ( Map.of ( "error", message ) );
                    } else if ( "Request body cannot be empty".equalsIgnoreCase ( message ) ) {
                        return ServerResponse.status ( HttpStatus.BAD_REQUEST )
                                .contentType ( MediaType.APPLICATION_JSON )
                                .bodyValue ( Map.of ( "error", message ) );
                    }

                    return ServerResponse.status ( HttpStatus.BAD_REQUEST )
                            .contentType ( MediaType.APPLICATION_JSON )
                            .bodyValue ( Map.of ( "error", message ) );
                } )
                .doOnNext ( resp -> log.info( "[TRACE] Login attempt processed" ) );
    }

    public Mono<ServerResponse> validateToken(ServerRequest request) {
        String authHeader = request.headers().firstHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ServerResponse.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("error", "Missing or invalid Authorization header"));
        }

        String token = authHeader.substring(7);

        if (!jwtService.isTokenValid(token)) {
            return ServerResponse.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("error", "Invalid or expired token"));
        }

        UUID idUser = jwtService.extractUserId(token);
        UUID idRol = jwtService.extractRoleId(token);

        AuthResponseDTO response = AuthResponseDTO.builder()
                .idUser(idUser)
                .idRol(idRol)
                .token (token)
                .build();

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(response);
    }
}
