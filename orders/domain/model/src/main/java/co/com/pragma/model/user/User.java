package co.com.pragma.model.user;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder(toBuilder = true)
public class User {
    String firstName;
    String lastName;
    String emailAddress;
    BigDecimal baseSalary;
}
