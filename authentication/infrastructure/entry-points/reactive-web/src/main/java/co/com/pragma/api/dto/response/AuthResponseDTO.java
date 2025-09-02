package co.com.pragma.api.dto.response;

import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AuthResponseDTO {
    UUID idUser;
    UUID idRol;
    String name;
    String token;
}
