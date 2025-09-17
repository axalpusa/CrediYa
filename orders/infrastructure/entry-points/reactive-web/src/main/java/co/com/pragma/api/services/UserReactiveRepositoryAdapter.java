package co.com.pragma.api.services;

import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class UserReactiveRepositoryAdapter implements UserRepository {

    private final AuthServiceClient authServiceClient;

    public UserReactiveRepositoryAdapter(AuthServiceClient authServiceClient) {
        this.authServiceClient = authServiceClient;
    }

    @Override
    public Mono<User> findByEmail(String emailAddress,String token) {
        return authServiceClient.getUserByEmailAddress(emailAddress,token);
    }
}
