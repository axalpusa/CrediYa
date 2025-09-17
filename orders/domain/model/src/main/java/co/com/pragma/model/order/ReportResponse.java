package co.com.pragma.model.order;

import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class ReportResponse {
    private BigDecimal amount;
    private Integer termMonths;
    private String email;
    private String name;
    private String typeLoan;
    private BigDecimal interestRate;
    private String statusOrder;
    private BigDecimal baseSalary;
    private BigDecimal totalMonthlyDebtApprovedRequests;
}
