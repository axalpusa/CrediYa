package co.com.pragma.model.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CapacityResponse {
    private String traceId;
    private Result result;
    private OriginalOrder originalOrder;

    @Data
    public static class Result {
        private String orderId;
        private String decision;
        private Double maxCapacity;
        private Double debtMonthly;
        private Double availableCapacity;
        private Double cuotaNuevo;
        private Double cuotaCalculada;
        private List < Payment > paymentPlan;
    }

    @Data
    public static class Payment {
        private int month;
        private Double capital;
        private Double interest;
        private Double installment;
        private Double remainingBalance;
    }

    @Data
    public static class OriginalOrder {
        private String idOrder;
        private Double amount;
        private Integer termMonths;
        private Double interestRate;
        private String emailAddress;
    }
}
