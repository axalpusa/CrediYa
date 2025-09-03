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
    private static final String baseURL = "/api/v1";

    public static final String LOGIN = baseURL + "/auth/login";
    public static final String VALIDATE = baseURL + "/auth/validate";

    public static final String USERS = baseURL + "/users";
    public static final String USERSBYID = baseURL + "/users/{idUser}";
    public static final String USERSALL = baseURL + "/users/all";
    public static final String USERSBYEMAIL = baseURL + "/users/byEmail/{email}";

    public static final String ROL = baseURL + "/rol";
    public static final String ROLBYID = baseURL + "/rol/{idRol}";
    public static final String ROLLALL = baseURL + "/rol/all";

}