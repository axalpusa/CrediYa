package co.com.pragma.api.dto.request;

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

    String firstName;
    String lastName;
    String emailAddress;
    String address;
    String documentId;
    LocalDate birthDate;
    String phoneNumber;
    String password;
    BigDecimal baseSalary;
    UUID idRol;

}
