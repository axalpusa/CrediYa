package co.com.pragma.api.services;

import co.com.pragma.api.config.ApiPaths;
import co.com.pragma.api.dto.response.AuthResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class AuthServiceClient {

    private final WebClient webClient;

    public AuthServiceClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://localhost:8090").build();
    }

    public Mono<AuthResponseDTO> validateToken(String token) {
        return webClient.get()
                .uri(ApiPaths.VALIDATE)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(AuthResponseDTO.class);
    }

    public Mono<AuthResponseDTO> validateToken(String token) {
        return webClient.get()
                .uri(ApiPaths.VALIDATE)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(AuthResponseDTO.class);
    }


}
