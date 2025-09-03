package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.request.AuthRequestDTO;
import co.com.pragma.api.dto.response.AuthResponseDTO;
import co.com.pragma.model.auth.Auth;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapperDTO {

   /* AuthResponseDTO toResponse(Auth auth);

    @Mapping(target = "idUser", ignore = true)
    Auth toModel(AuthRequestDTO authRequestDTO);*/
}
