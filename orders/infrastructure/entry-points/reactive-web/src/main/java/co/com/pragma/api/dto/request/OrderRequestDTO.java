package co.com.pragma.api.dto.request;

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
public class OrderRequestDTO {

    BigDecimal amount;
    Integer termMonths;
    String documentId;
    String emailAddress;
    UUID idTypeLoan;
    UUID idStatus;

}
