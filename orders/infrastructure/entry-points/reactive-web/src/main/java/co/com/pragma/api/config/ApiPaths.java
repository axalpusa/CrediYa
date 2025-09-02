package co.com.pragma.api.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.stereotype.Component;

@Configuration
//@EnableWebFluxSecurity
@AllArgsConstructor
@Component
public class ApiPaths {
    public static final String VALIDATE = "/api/v1/auth/validate";

    public static final String TYPELOAN = "/api/v1/typeLoan";
    public static final String TYPELOANBYID = "/api/v1/typeLoan/{idTypeLoan}";
    public static final String TYPELOANSALL = "/api/v1/typeLoan/all";

    public static final String STATUS = "/api/v1/status";
    public static final String STATUSBYID = "/api/v1/status/{idStatus}";
    public static final String STATUSALL = "/api/v1/status/all";

    public static final String ORDER = "/api/v1/order";
    public static final String ORDERBYID = "/api/v1/order/{idOrder}";
    public static final String ORDERALL = "/api/v1/order/all";

}