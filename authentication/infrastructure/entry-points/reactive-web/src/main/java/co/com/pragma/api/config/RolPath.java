package co.com.pragma.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "routes.paths")
public class RolPath {
    private String rol;
    private String rolById;
    private String rolAll;
}
