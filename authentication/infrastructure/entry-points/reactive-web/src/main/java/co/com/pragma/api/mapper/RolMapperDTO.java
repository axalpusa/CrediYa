package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.request.RolRequestDTO;
import co.com.pragma.api.dto.response.RolResponseDTO;
import co.com.pragma.model.rol.Rol;
import co.com.pragma.model.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RolMapperDTO {

    RolResponseDTO toResponse(Rol rol);

    @Mapping(target = "idRol", ignore = true)
    Rol toModel(RolRequestDTO rolRequestDTO);
}
