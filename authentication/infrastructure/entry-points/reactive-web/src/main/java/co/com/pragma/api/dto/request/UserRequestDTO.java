package co.com.pragma.api.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserRequestDTO {

    @NotBlank(message = "First name is required")
    String firstName;

    @NotBlank(message = "Last name is required")
    String lastName;

    @NotBlank(message = "Email address is required")
    @Email(message = "El correo electrónico no tiene un formato válido")
    String emailAddress;

    String address;

    @NotBlank(message = "Document id is required")
    String documentId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    LocalDate birthDate;
    String phoneNumber;

    String password;

    @NotNull(message = "Base salary is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "The salary cannot be negative")
    @DecimalMax(value = "15000000.0", inclusive = true, message = "The salary cannot be 15M")
    BigDecimal baseSalary;

    @NotNull(message = "Rol is required")
    UUID idRol;

}
