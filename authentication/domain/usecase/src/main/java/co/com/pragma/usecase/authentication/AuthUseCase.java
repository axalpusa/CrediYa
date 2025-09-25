package co.com.pragma.usecase.authentication;

import co.com.pragma.model.auth.Auth;
import co.com.pragma.usecase.user.UserUseCase;
import exceptions.NotFoundPragmaException;
import exceptions.UnauthorizedPragmaException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.function.BiFunction;

import static constants.Constants.INVALID_CREDENTIALS;
import static constants.Constants.USER_NOT_FOUND;

@RequiredArgsConstructor
public class AuthUseCase {

    private final UserUseCase userUseCase;

    public Mono<Auth> login(String email, String password,
                            BiFunction<UUID, UUID, String> tokenGenerator,
                            BiFunction<String, String, Boolean> passwordMatches) {
        return userUseCase.findByEmailAddress(email)
                .switchIfEmpty(Mono.error(new NotFoundPragmaException (USER_NOT_FOUND)))
                .flatMap(user -> {
                    if (!passwordMatches.apply(password, user.getPassword())) {
                        return Mono.error(new UnauthorizedPragmaException (INVALID_CREDENTIALS));
                    }
                    String token = tokenGenerator.apply(user.getIdUser(), user.getIdRol());
                    return Mono.just(new Auth(user.getIdUser(), user.getIdRol(), user.getFirstName(), token));
                });
    }

}
