package co.com.pragma.r2dbc.config;

// TODO: Load properties from the application.yaml file or from secrets manager
// import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.boot.context.properties.ConfigurationProperties;

//@ConstructorBinding
@ConfigurationProperties(prefix = "spring.r2dbc")
public record PostgresqlConnectionProperties(
        String host,
        Integer port,
        String database,
        String schema,
        String username,
        String password) {
}
