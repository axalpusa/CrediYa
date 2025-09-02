package co.com.pragma.api.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.stereotype.Component;

@Configuration
@EnableWebFluxSecurity
@AllArgsConstructor
@Component
public class ApiPaths {
    public static final String LOGIN = "/api/v1/auth/login";
    public static final String VALIDATE = "/api/v1/auth/validate";

    public static final String USERS = "/api/v1/users";
    public static final String USERSBYID = "/api/v1/users/{idUser}";
    public static final String USERSALL = "/api/v1/users/all";

    public static final String ROL = "/api/v1/rol";
    public static final String ROLBYID = "/api/v1/rol/{idRol}";
    public static final String ROLLALL = "/api/v1/rol/all";

}