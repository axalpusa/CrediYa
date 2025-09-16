package co.com.pragma.model.order;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderTest {

    @Test
    void merge_shouldUpdateOnlyNonNullValues() {
        UUID idOrder = UUID.randomUUID ( );
        UUID idStatus = UUID.randomUUID ( );
        UUID idTypeLoan = UUID.randomUUID ( );

        Order original = Order.builder ( )
                .idOrder ( idOrder )
                .amount ( BigDecimal.valueOf ( 5000 ) )
                .termMonths ( 12 )
                .documentId ( "12345678" )
                .emailAddress ( "test@original.com" )
                .idStatus ( idStatus )
                .idTypeLoan ( idTypeLoan )
                .build ( );

        Order other = Order.builder ( )
                .amount ( BigDecimal.valueOf ( 7000 ) )
                .documentId ( "87654321" )
                .termMonths ( 24 )
                .emailAddress ( "updated@test.com" )
                .idStatus ( UUID.randomUUID ( ) )
                .idTypeLoan ( UUID.randomUUID ( ) )
                .build ( );

        original.merge ( other );

        assertThat ( original.getAmount ( ) ).isEqualByComparingTo ( BigDecimal.valueOf ( 7000 ) );
        assertThat ( original.getDocumentId ( ) ).isEqualTo ( "87654321" );
        assertThat ( original.getTermMonths ( ) ).isEqualTo ( 24 );
        assertThat ( original.getEmailAddress ( ) ).isEqualTo ( "updated@test.com" );
        assertThat ( original.getIdStatus ( ) ).isEqualTo ( other.getIdStatus ( ) );
        assertThat ( original.getIdTypeLoan ( ) ).isEqualTo ( other.getIdTypeLoan ( ) );
    }

    @Test
    void merge_shouldDoNothing_whenOtherHasAllNullValues() {
        UUID idOrder = UUID.randomUUID ( );
        UUID idStatus = UUID.randomUUID ( );
        UUID idTypeLoan = UUID.randomUUID ( );

        Order original = Order.builder ( )
                .idOrder ( idOrder )
                .amount ( BigDecimal.valueOf ( 4000 ) )
                .termMonths ( 6 )
                .documentId ( "11223344" )
                .emailAddress ( "nochange@test.com" )
                .idStatus ( idStatus )
                .idTypeLoan ( idTypeLoan )
                .build ( );

        Order other = new Order ( );

        original.merge ( other );

        // No cambia nada
        assertThat ( original.getIdOrder ( ) ).isEqualTo ( idOrder );
        assertThat ( original.getAmount ( ) ).isEqualByComparingTo ( BigDecimal.valueOf ( 4000 ) );
        assertThat ( original.getTermMonths ( ) ).isEqualTo ( 6 );
        assertThat ( original.getDocumentId ( ) ).isEqualTo ( "11223344" );
        assertThat ( original.getEmailAddress ( ) ).isEqualTo ( "nochange@test.com" );
        assertThat ( original.getIdStatus ( ) ).isEqualTo ( idStatus );
        assertThat ( original.getIdTypeLoan ( ) ).isEqualTo ( idTypeLoan );
    }

    @Test
    void builderAndGettersSetters_shouldWorkCorrectly() {
        UUID idOrder = UUID.randomUUID ( );
        UUID idStatus = UUID.randomUUID ( );
        UUID idTypeLoan = UUID.randomUUID ( );

        Order order = new Order ( );
        order.setIdOrder ( idOrder );
        order.setAmount ( BigDecimal.valueOf ( 2500 ) );
        order.setTermMonths ( 18 );
        order.setDocumentId ( "44556677" );
        order.setEmailAddress ( "setter@test.com" );
        order.setIdStatus ( idStatus );
        order.setIdTypeLoan ( idTypeLoan );

        assertThat ( order.getIdOrder ( ) ).isEqualTo ( idOrder );
        assertThat ( order.getAmount ( ) ).isEqualByComparingTo ( BigDecimal.valueOf ( 2500 ) );
        assertThat ( order.getTermMonths ( ) ).isEqualTo ( 18 );
        assertThat ( order.getDocumentId ( ) ).isEqualTo ( "44556677" );
        assertThat ( order.getEmailAddress ( ) ).isEqualTo ( "setter@test.com" );
        assertThat ( order.getIdStatus ( ) ).isEqualTo ( idStatus );
        assertThat ( order.getIdTypeLoan ( ) ).isEqualTo ( idTypeLoan );
    }
}
