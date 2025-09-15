package co.com.pragma.api;

import co.com.pragma.api.dto.request.OrderRequestDTO;
import co.com.pragma.api.dto.request.StatusRequestDTO;
import co.com.pragma.api.dto.request.TypeLoanRequestDTO;
import co.com.pragma.api.dto.response.OrderResponseDTO;
import co.com.pragma.api.dto.response.StatusResponseDTO;
import co.com.pragma.api.dto.response.TypeLoanResponseDTO;
import co.com.pragma.api.mapper.OrderMapperDTO;
import co.com.pragma.api.mapper.OrderMapperDTOImpl;
import co.com.pragma.api.mapper.StatusMapperDTO;
import co.com.pragma.api.mapper.StatusMapperDTOImpl;
import co.com.pragma.api.mapper.TypeLoanMapperDTO;
import co.com.pragma.api.mapper.TypeLoanMapperDTOImpl;
import co.com.pragma.model.order.Order;
import co.com.pragma.model.status.Status;
import co.com.pragma.model.typeloan.TypeLoan;
import enums.StatusEnum;
import enums.TypeLoanEnum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapperDTOTest {

    private final TypeLoanMapperDTO typeLoanMapper = new TypeLoanMapperDTOImpl ( );
    private final StatusMapperDTO statusMapper = new StatusMapperDTOImpl ( );
    private final OrderMapperDTO orderMapper = new OrderMapperDTOImpl ( );

    @Test
    void typeLoan_toResponse_shouldMapAllFields() {
        UUID id = UUID.randomUUID ( );

        TypeLoan typeLoan = TypeLoan.builder ( )
                .idTypeLoan ( id )
                .name ( "nam" )
                .automaticValidation ( true )
                .maximumAmount ( new BigDecimal ( 5000.00 ) )
                .minimumAmount ( new BigDecimal ( 100 ) )
                .interestRate ( new BigDecimal ( 0.5 ) )
                .build ( );

        TypeLoanResponseDTO dto = typeLoanMapper.toResponse ( typeLoan );

        assertEquals ( id, dto.getIdTypeLoan ( ) );
        assertEquals ( "nam", dto.getName ( ) );
        assertEquals ( new BigDecimal ( 100.00 ), dto.getMinimumAmount ( ) );
        assertEquals ( new BigDecimal ( 5000.00 ), dto.getMaximumAmount ( ) );
        assertEquals ( new BigDecimal ( 0.5 ), dto.getInterestRate ( ) );
        assertEquals ( true, dto.getAutomaticValidation ( ) );
    }

    @Test
    void typeLoan_toModel_shouldMapAllFields() {
        TypeLoanRequestDTO request = new TypeLoanRequestDTO ( );
        request.setName ( "name" );
        request.setMinimumAmount ( new BigDecimal ( 100.00 ) );
        request.setMaximumAmount ( new BigDecimal ( 5000.00 ) );
        request.setInterestRate ( new BigDecimal ( 0.5 ) );
        request.setAutomaticValidation ( true );

        TypeLoan typeLoan = typeLoanMapper.toModel ( request );

        assertEquals ( "name", typeLoan.getName ( ) );
        assertEquals ( new BigDecimal ( 100.00 ), typeLoan.getMinimumAmount ( ) );
        assertEquals ( new BigDecimal ( 5000.00 ), typeLoan.getMaximumAmount ( ) );
        assertEquals ( new BigDecimal ( 0.5 ), typeLoan.getInterestRate ( ) );
        assertEquals ( true, typeLoan.getAutomaticValidation ( ) );
    }

    @Test
    void status_toResponse_shouldMapAllFields() {
        UUID id = UUID.randomUUID ( );

        Status status = Status.builder ( )
                .idStatus ( id )
                .name ( "status" )
                .description ( "des" )
                .build ( );

        StatusResponseDTO dto = statusMapper.toResponse ( status );

        assertEquals ( id, dto.getIdStatus ( ) );
        assertEquals ( "status", dto.getName ( ) );
        assertEquals ( "des", dto.getDescription ( ) );
    }

    @Test
    void status_toModel_shouldMapAllFields() {
        StatusRequestDTO request = new StatusRequestDTO ( );
        request.setName ( "status" );
        request.setDescription ( "des" );

        Status status = statusMapper.toModel ( request );

        assertEquals ( "status", status.getName ( ) );
        assertEquals ( "des", status.getDescription ( ) );
    }

    @Test
    void order_toResponse_shouldMapAllFields() {
        UUID id = UUID.randomUUID ( );

        Order order = Order.builder ( )
                .idOrder ( id )
                .documentId ( "48295730" )
                .termMonths (12 )
                .amount (new BigDecimal ( 1000.00 ))
                .emailAddress ( "axalpusa@gmail.com" )
                .idTypeLoan ( TypeLoanEnum.TYPE1.getId ( ) )
                .idStatus ( StatusEnum.PENDENT.getId ( ) )
                .build ( );

        OrderResponseDTO dto = orderMapper.toResponse ( order );

        assertEquals ( id, dto.getIdOrder ( ) );
        assertEquals ( "48295730", dto.getDocumentId ( ) );
    }

    @Test
    void order_toModel_shouldMapAllFields() {
        OrderRequestDTO request = new OrderRequestDTO ( );
        UUID idStatus = UUID.randomUUID ( );
        UUID idTypeLoan = UUID.randomUUID ( );
        request.setDocumentId ( "48295730" );
        request.setTermMonths (12 );
        request.setAmount (new BigDecimal ( 1000.00 ));
        request.setEmailAddress ( "axalpusa@gmail.com" );
        request.setIdTypeLoan (idTypeLoan  );
        request.setIdStatus ( idStatus );

        Order status = orderMapper.toModel ( request );

        assertEquals ( "48295730", status.getDocumentId ( ) );
    }

}
