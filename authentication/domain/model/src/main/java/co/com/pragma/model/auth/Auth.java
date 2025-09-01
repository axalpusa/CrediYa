package co.com.pragma.model.auth;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Auth {
    private UUID idUser;
    private UUID idRol;
    private String name;
    private String token;
}
