package co.com.pragma.api.handler;

import co.com.pragma.api.jwt.JwtService;
import co.com.pragma.api.dto.request.LoginRequestDTO;
import co.com.pragma.usecase.authentication.AuthUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthHandler {

    private final AuthUseCase authUseCase;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public Mono<ServerResponse> login(ServerRequest request) {
        return request.bodyToMono(LoginRequestDTO.class)
                .switchIfEmpty(Mono.error(new RuntimeException("Request body cannot be empty")))
                .flatMap(dto -> authUseCase.login(
                dto.getEmail(),
                dto.getPassword(),
                (userId, roleId) -> jwtService.generateToken(userId, roleId),
                passwordEncoder::matches
        ))
                .flatMap(authResult -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(authResult))
                .onErrorResume(e -> ServerResponse
                        .badRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(Map.of("error", e.getMessage())))
                .doOnNext(resp -> System.out.println("[TRACE] Login attempt processed "));
    }


}
