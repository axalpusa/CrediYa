package co.com.pragma.model.order;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class CalculateCapacity {
    Order order;
    List<ReportResponse> reportResponses;
}
