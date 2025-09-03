package co.com.pragma.api.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@AllArgsConstructor
@Component
public class ApiPaths {
    private static final String baseURL = "/api/v1";

    public static final String VALIDATE = baseURL + "/auth/validate";
    public static final String USERSBYEMAIL = baseURL + "/users/byEmail/{email}";

    public static final String TYPELOAN = baseURL + "/typeLoan";
    public static final String TYPELOANBYID = baseURL + "/typeLoan/{idTypeLoan}";
    public static final String TYPELOANSALL = baseURL + "/typeLoan/all";

    public static final String STATUS = baseURL + "/status";
    public static final String STATUSBYID = baseURL + "/status/{idStatus}";
    public static final String STATUSALL = baseURL + "/status/all";

    public static final String ORDER = baseURL + "/order";
    public static final String ORDERBYID = baseURL + "/order/{idOrder}";
    public static final String ORDERALL = baseURL + "/order/all";
    public static final String REPORT = baseURL + "/order/report";

}