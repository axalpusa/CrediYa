package co.com.pragma.model.typeloan;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class TypeLoanTest {

    @Test
    void merge_shouldUpdateOnlyNonNullValues() {
        UUID id = UUID.randomUUID ( );
        TypeLoan original = TypeLoan.builder ( )
                .idTypeLoan ( id )
                .name ( "Personal Loan" )
                .minimumAmount ( BigDecimal.valueOf ( 1000 ) )
                .maximumAmount ( BigDecimal.valueOf ( 5000 ) )
                .interestRate ( BigDecimal.valueOf ( 5.5 ) )
                .automaticValidation ( false )
                .build ( );

        TypeLoan other = TypeLoan.builder ( )
                .name ( "Updated Loan" )
                .maximumAmount ( BigDecimal.valueOf ( 8000 ) )
                .minimumAmount ( BigDecimal.valueOf ( 2000 ) )
                .interestRate ( BigDecimal.valueOf ( 7.5 ) )
                .automaticValidation ( true )
                .build ( );

        original.merge ( other );

        assertThat ( original.getName ( ) ).isEqualTo ( "Updated Loan" );
        assertThat ( original.getMaximumAmount ( ) ).isEqualByComparingTo ( BigDecimal.valueOf ( 8000 ) );
        assertThat ( original.getInterestRate ( ) ).isEqualByComparingTo ( BigDecimal.valueOf ( 7.5 ) );

        assertThat ( original.getIdTypeLoan ( ) ).isEqualTo ( id );
        assertThat ( original.getMinimumAmount ( ) ).isEqualByComparingTo ( BigDecimal.valueOf ( 2000 ) );
        assertThat ( original.getAutomaticValidation ( ) ).isTrue ( );
    }

    @Test
    void merge_shouldDoNothing_whenOtherHasAllNullValues() {
        TypeLoan original = TypeLoan.builder ( )
                .name ( "Initial Loan" )
                .minimumAmount ( BigDecimal.valueOf ( 2000 ) )
                .maximumAmount ( BigDecimal.valueOf ( 4000 ) )
                .interestRate ( BigDecimal.valueOf ( 6.0 ) )
                .automaticValidation ( true )
                .build ( );

        TypeLoan other = new TypeLoan ( );

        original.merge ( other );

        assertThat ( original.getName ( ) ).isEqualTo ( "Initial Loan" );
        assertThat ( original.getMinimumAmount ( ) ).isEqualByComparingTo ( BigDecimal.valueOf ( 2000 ) );
        assertThat ( original.getMaximumAmount ( ) ).isEqualByComparingTo ( BigDecimal.valueOf ( 4000 ) );
        assertThat ( original.getInterestRate ( ) ).isEqualByComparingTo ( BigDecimal.valueOf ( 6.0 ) );
        assertThat ( original.getAutomaticValidation ( ) ).isTrue ( );
    }
}
