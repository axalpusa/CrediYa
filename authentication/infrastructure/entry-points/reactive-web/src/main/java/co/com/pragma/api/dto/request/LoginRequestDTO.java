package co.com.pragma.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class LoginRequestDTO {

    @NotBlank(message = "Email is required")
    String email;

    @NotBlank(message = "Password is required")
    String password;

}
