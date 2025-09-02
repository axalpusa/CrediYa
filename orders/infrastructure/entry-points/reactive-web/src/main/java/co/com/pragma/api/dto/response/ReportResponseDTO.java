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
public class ReportResponseDTO {
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
