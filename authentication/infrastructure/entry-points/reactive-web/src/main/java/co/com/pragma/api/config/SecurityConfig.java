package co.com.pragma.api.config;

import co.com.pragma.api.enums.RolEnum;
import co.com.pragma.api.jwt.JwtAuthenticationFilter;
import co.com.pragma.api.jwt.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Configuration
@EnableWebFluxSecurity
@AllArgsConstructor
@Component
public class SecurityConfig {
    private final JwtService jwtService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable) // deshabilita basic
                .authorizeExchange(auth -> auth
                        // permitir login sin token
                        .pathMatchers(HttpMethod.POST, "/api/v1/login").permitAll()
                        // registrar usuarios: solo ADMIN o ASESOR
                        .pathMatchers(HttpMethod.POST, "/api/v1/users").access(this::isAdminOrAsesor)
                        // crear préstamos: solo CLIENTE y para sí mismo
                        .pathMatchers(HttpMethod.POST, "/api/v1/order/{userId}").access(this::isClientForSelf)
                        // el resto requiere autenticación
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtService);
    }

    private Mono<AuthorizationDecision> isAdminOrAsesor(Mono<Authentication> authenticationMono,
                                                        AuthorizationContext context) {
        return authenticationMono
                .doOnNext(auth -> System.out.println("[DEBUG] Authentication object: " + auth))
                .map(auth -> {
                    // Revisar credenciales
                    Object credentials = auth.getCredentials();
                    if (credentials == null) {
                        System.out.println("[DEBUG] No credentials found, denying access");
                        return new AuthorizationDecision(false);
                    }
                    String token = (String) credentials;
                    UUID roleId = jwtService.extractRoleId(token);
                    boolean allowed = roleId.equals(RolEnum.ADMIN.getId()) || roleId.equals(RolEnum.ASSESSOR.getId());
                    return new AuthorizationDecision(allowed);
                })
                .defaultIfEmpty(new AuthorizationDecision(false)) // si no hay auth, negar
                .doOnNext(decision -> System.out.println("[DEBUG] AuthorizationDecision: " + decision.isGranted()));
    }


    private Mono<AuthorizationDecision> isClientForSelf(Mono<Authentication> authenticationMono,
                                                        AuthorizationContext context) {
        return authenticationMono.map(auth -> {
            String token = (String) auth.getCredentials();
            UUID roleId = jwtService.extractRoleId(token);
            UUID userId = jwtService.extractUserId(token);

            // userId del path (ej: /loans/create/{userId})
            String pathUserId = context.getVariables().get("userId").toString();

            boolean allowed = roleId.equals(RolEnum.CLIENT.getId()) && userId.toString().equals(pathUserId);
            return new AuthorizationDecision(allowed);
        }).defaultIfEmpty(new AuthorizationDecision(false));
    }

}