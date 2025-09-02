package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.request.StatusRequestDTO;
import co.com.pragma.api.dto.response.StatusResponseDTO;
import co.com.pragma.model.status.Status;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StatusMapperDTO {

    StatusResponseDTO toResponse(Status status);

    @Mapping(target = "idStatus", ignore = true)
    Status toModel(StatusRequestDTO statusRequestDTO);
}
