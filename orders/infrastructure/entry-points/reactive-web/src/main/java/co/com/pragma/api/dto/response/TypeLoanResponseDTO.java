package co.com.pragma.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TypeLoanResponseDTO {
    UUID idTypeLoan;
    String name;
    BigDecimal minimumAmount;
    BigDecimal maximumAmount;
    BigDecimal interestRate;
    Boolean automaticValidation;
}
