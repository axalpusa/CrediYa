package co.com.pragma.api.config;

import co.com.pragma.api.enums.RolEnum;
import co.com.pragma.api.jwt.JwtAuthenticationFilter;
import co.com.pragma.api.jwt.JwtService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Configuration
@EnableWebFluxSecurity
@AllArgsConstructor
@Component
public class SecurityConfig {
    private final JwtService jwtService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder ( );
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter ( jwtService );
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf ( ServerHttpSecurity.CsrfSpec::disable )
                .httpBasic ( ServerHttpSecurity.HttpBasicSpec::disable )
                .authorizeExchange ( auth -> {
                    configurePublicEndpoints ( auth );
                    configureUserEndpoints ( auth );
                    configureRolEndpoints ( auth );
                    configureOtherEndpoints ( auth );
                } )
                .addFilterAt ( jwtAuthenticationFilter ( ), SecurityWebFiltersOrder.AUTHENTICATION )
                .build ( );
    }

    private void configurePublicEndpoints(ServerHttpSecurity.AuthorizeExchangeSpec auth) {
        auth.pathMatchers ( HttpMethod.POST, ApiPaths.LOGIN ).permitAll ( );
        auth.pathMatchers (
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/api/doc/**",
                "/api/doc/swagger-ui.html",
                "/api/doc/api-docs",
                "/webjars/swagger-ui/**"
        ).permitAll ( );

    }

    private void configureUserEndpoints(ServerHttpSecurity.AuthorizeExchangeSpec auth) {
        auth.pathMatchers ( HttpMethod.POST, ApiPaths.USERS ).access ( this::isAdminOrAsesor );
        auth.pathMatchers ( HttpMethod.PUT, ApiPaths.USERS ).access ( this::isAdminOrAsesor );
        auth.pathMatchers ( HttpMethod.DELETE, ApiPaths.USERSBYID ).access ( this::isAdminOrAsesor );
        auth.pathMatchers ( HttpMethod.GET, ApiPaths.USERSALL ).access ( this::isAdminOrAsesor );
        auth.pathMatchers ( HttpMethod.GET, ApiPaths.USERSBYID ).access ( this::isAdminOrAsesor );
    }

    private void configureRolEndpoints(ServerHttpSecurity.AuthorizeExchangeSpec auth) {
        auth.pathMatchers ( HttpMethod.POST, ApiPaths.ROL ).access ( this::isAdminOrAsesor );
        auth.pathMatchers ( HttpMethod.PUT, ApiPaths.ROL ).access ( this::isAdminOrAsesor );
        auth.pathMatchers ( HttpMethod.DELETE, ApiPaths.ROLBYID ).access ( this::isAdminOrAsesor );
        auth.pathMatchers ( HttpMethod.GET, ApiPaths.ROLLALL ).access ( this::isAdminOrAsesor );
        auth.pathMatchers ( HttpMethod.GET, ApiPaths.ROLBYID ).access ( this::isAdminOrAsesor );
    }

    private void configureOtherEndpoints(ServerHttpSecurity.AuthorizeExchangeSpec auth) {
        auth.anyExchange ( ).authenticated ( );
    }

    private Mono < AuthorizationDecision > isAdminOrAsesor(Mono < Authentication > authenticationMono,
                                                           AuthorizationContext context) {
        return authenticationMono
                .doOnNext ( auth -> log.info ( "[DEBUG] Authentication object: " + auth ) )
                .map ( auth -> {
                    Object credentials = auth.getCredentials ( );
                    if ( credentials == null ) {
                        log.info ( "[DEBUG] No credentials found, denying access" );
                        return new AuthorizationDecision ( false );
                    }
                    String token = (String) credentials;
                    UUID roleId = jwtService.extractRoleId ( token );
                    boolean allowed = roleId.equals ( RolEnum.ADMIN.getId ( ) ) || roleId.equals ( RolEnum.ASSESSOR.getId ( ) );
                    return new AuthorizationDecision ( allowed );
                } )
                .defaultIfEmpty ( new AuthorizationDecision ( false ) )
                .doOnNext ( decision -> log.info ( "[DEBUG] AuthorizationDecision: " + decision.isGranted ( ) ) );
    }


}