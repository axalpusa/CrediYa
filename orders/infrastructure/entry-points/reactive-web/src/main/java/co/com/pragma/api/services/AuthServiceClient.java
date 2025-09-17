package co.com.pragma.api.services;

import co.com.pragma.api.config.ApiPaths;
import co.com.pragma.api.dto.response.AuthResponseDTO;
import co.com.pragma.model.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class AuthServiceClient {

    private final WebClient webClient;

    public AuthServiceClient(WebClient.Builder builder,
                             @Value("${auth.service.url}") String authServiceUrl) {
        this.webClient = builder
                .baseUrl(authServiceUrl)
                .build();
    }

    public Mono<AuthResponseDTO> validateToken(String token) {
        return webClient.get()
                .uri(ApiPaths.VALIDATE)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(AuthResponseDTO.class);
    }

    public Mono<User> getUserByEmailAddress(String email, String token) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiPaths.USERSBYEMAIL)
                        .build(email))
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(User.class);
    }


}
