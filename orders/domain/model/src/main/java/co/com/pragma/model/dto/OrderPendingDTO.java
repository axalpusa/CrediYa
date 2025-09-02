package co.com.pragma.model.dto;

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
public class OrderPendingDTO {
    private BigDecimal amount;
    private Integer termMonths;
    private String email;
    private String typeLoan;
    private BigDecimal interestRate;
    private String statusOrder;
    private BigDecimal totalMonthlyDebtApprovedRequests;
}

