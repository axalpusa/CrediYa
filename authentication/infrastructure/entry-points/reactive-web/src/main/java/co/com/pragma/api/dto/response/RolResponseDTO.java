package co.com.pragma.api.dto.response;

import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RolResponseDTO {
    UUID idRol;
    String name;
    String description;
}
