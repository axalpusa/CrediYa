package co.com.pragma.model.order;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder(toBuilder = true)
public class Order {
    private UUID idOrder;
    private BigDecimal amount;
    private Integer termMonths;
    private String documentId;
    private String emailAddress;
    private UUID idStatus;
    private UUID idTypeLoan;

    public void merge(Order other) {
        if ( other.getAmount ( ) != null ) this.amount = other.getAmount ( );
        if ( other.getDocumentId ( ) != null ) this.documentId = other.getDocumentId ( );
        if ( other.getTermMonths ( ) != null ) this.termMonths = other.getTermMonths ( );
        if ( other.getEmailAddress ( ) != null ) this.emailAddress = other.getEmailAddress ( );
        if ( other.getIdStatus ( ) != null ) this.idStatus = other.getIdStatus ( );
        if ( other.getIdTypeLoan ( ) != null ) this.idTypeLoan = other.getIdTypeLoan ( );
    }
}
