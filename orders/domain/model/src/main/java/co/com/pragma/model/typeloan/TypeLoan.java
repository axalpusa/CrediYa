package co.com.pragma.model.typeloan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class TypeLoan {
    private UUID idTypeLoan;
    private String name;
    private BigDecimal minimumAmount;
    private BigDecimal maximumAmount;
    private BigDecimal interestRate;
    private Boolean automaticValidation;

    public void merge(TypeLoan other) {
        if ( other.getName ( ) != null ) this.name = other.getName ( );
        if ( other.getMinimumAmount ( ) != null ) this.minimumAmount = other.getMinimumAmount ( );
        if ( other.getMaximumAmount ( ) != null ) this.maximumAmount = other.getMaximumAmount ( );
        if ( other.getInterestRate ( ) != null ) this.interestRate = other.getInterestRate ( );
        if ( other.getAutomaticValidation ( ) != null ) this.automaticValidation = other.getAutomaticValidation ( );
    }
}
