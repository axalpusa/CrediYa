package co.com.pragma.model.order;

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
public class Order {
    private UUID idOrder;
    private BigDecimal amount;
    private Integer termMonths;
    private String documentId;
    private String emailAddress;
    private UUID idStatus;
    private UUID idTypeLoan;
}
