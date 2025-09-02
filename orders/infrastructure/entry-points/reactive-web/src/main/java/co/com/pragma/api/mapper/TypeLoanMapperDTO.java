package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.request.TypeLoanRequestDTO;
import co.com.pragma.api.dto.response.TypeLoanResponseDTO;
import co.com.pragma.model.typeloan.TypeLoan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TypeLoanMapperDTO {

    TypeLoanResponseDTO toResponse(TypeLoan typeLoan);

    @Mapping(target = "idTypeLoan", ignore = true)
    TypeLoan toModel(TypeLoanRequestDTO typeLoanRequestDTO);
}
