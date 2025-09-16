package co.com.pragma.model.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CapacityResponse {
    private UUID idOrder;
    private boolean approved;
    private boolean manualReview; // true si > 5 salarios
    private List < Installment > installments;

    public static class Installment {
        private int month;
        private BigDecimal capital;
        private BigDecimal interest;
    }
}
