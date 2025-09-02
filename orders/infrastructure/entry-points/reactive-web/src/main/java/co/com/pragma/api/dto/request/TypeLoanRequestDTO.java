package co.com.pragma.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TypeLoanRequestDTO {
    String name;
    BigDecimal minimumAmount;
    BigDecimal maximumAmount;
    BigDecimal interestRate;
    Boolean automaticValidation;
}

