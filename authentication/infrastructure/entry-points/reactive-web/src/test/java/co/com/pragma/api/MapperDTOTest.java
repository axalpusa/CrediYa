package co.com.pragma.api;

import co.com.pragma.api.dto.request.RolRequestDTO;
import co.com.pragma.api.dto.request.UserRequestDTO;
import co.com.pragma.api.dto.response.RolResponseDTO;
import co.com.pragma.api.dto.response.UserResponseDTO;
import co.com.pragma.api.mapper.RolMapperDTO;
import co.com.pragma.api.mapper.RolMapperDTOImpl;
import co.com.pragma.api.mapper.UserMapperDTO;
import co.com.pragma.api.mapper.UserMapperDTOImpl;
import co.com.pragma.model.rol.Rol;
import co.com.pragma.model.user.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapperDTOTest {

    private final UserMapperDTO userMapper = new UserMapperDTOImpl ( );
    private final RolMapperDTO rolMapper = new RolMapperDTOImpl ( );

    @Test
    void user_toResponse_shouldMapAllFields() {
        UUID id = UUID.randomUUID ( );
        UUID roleId = UUID.randomUUID ( );

        User user = User.builder ( )
                .idUser ( id )
                .firstName ( "Axel" )
                .lastName ( "Puertas" )
                .emailAddress ( "axel@example.com" )
                .address ( "Piura" )
                .documentId ( "48295730" )
                .birthDate ( LocalDate.of ( 1995, 1, 1 ) )
                .phoneNumber ( "973157252" )
                .password ( "1234" )
                .baseSalary ( new BigDecimal ( "5000" ) )
                .idRol ( roleId )
                .build ( );

        UserResponseDTO dto = userMapper.toResponse ( user );

        assertEquals ( id, dto.getIdUser ( ) );
        assertEquals ( "Axel", dto.getFirstName ( ) );
        assertEquals ( "Puertas", dto.getLastName ( ) );
        assertEquals ( "axel@example.com", dto.getEmailAddress ( ) );
        assertEquals ( "Piura", dto.getAddress ( ) );
        assertEquals ( "48295730", dto.getDocumentId ( ) );
        assertEquals ( LocalDate.of ( 1995, 1, 1 ), dto.getBirthDate ( ) );
        assertEquals ( "973157252", dto.getPhoneNumber ( ) );
        assertEquals ( new BigDecimal ( "5000" ), dto.getBaseSalary ( ) );
        assertEquals ( roleId, dto.getIdRol ( ) );
    }

    @Test
    void user_toModel_shouldMapAllFields() {
        UserRequestDTO request = new UserRequestDTO ( );
        request.setFirstName ( "Axel" );
        request.setLastName ( "Puertas" );
        request.setEmailAddress ( "axel@example.com" );
        request.setAddress ( "Piura" );
        request.setDocumentId ( "48295730" );
        request.setBirthDate ( LocalDate.of ( 1995, 1, 1 ) );
        request.setPhoneNumber ( "973157252" );
        request.setPassword ( "1234" );
        request.setBaseSalary ( new BigDecimal ( "5000" ) );
        request.setIdRol ( UUID.randomUUID ( ) );

        User user = userMapper.toModel ( request );

        assertEquals ( "Axel", user.getFirstName ( ) );
        assertEquals ( "Puertas", user.getLastName ( ) );
        assertEquals ( "axel@example.com", user.getEmailAddress ( ) );
        assertEquals ( "Piura", user.getAddress ( ) );
        assertEquals ( "48295730", user.getDocumentId ( ) );
        assertEquals ( LocalDate.of ( 1995, 1, 1 ), user.getBirthDate ( ) );
        assertEquals ( "973157252", user.getPhoneNumber ( ) );
        assertEquals ( "1234", user.getPassword ( ) );
        assertEquals ( new BigDecimal ( "5000" ), user.getBaseSalary ( ) );
        assertEquals ( request.getIdRol ( ), user.getIdRol ( ) );
    }

    @Test
    void rol_toResponse_shouldMapAllFields() {
        UUID id = UUID.randomUUID ( );

        Rol rol = Rol.builder ( )
                .idRol ( id )
                .name ( "rol" )
                .description ( "des" )
                .build ( );

        RolResponseDTO dto = rolMapper.toResponse ( rol );

        assertEquals ( id, dto.getIdRol ( ) );
        assertEquals ( "rol", dto.getName ( ) );
        assertEquals ( "des", dto.getDescription ( ) );
    }

    @Test
    void rol_toModel_shouldMapAllFields() {
        RolRequestDTO request = new RolRequestDTO ( );
        request.setName ( "rol" );
        request.setDescription ( "des" );

        Rol rol = rolMapper.toModel ( request );

        assertEquals ( "rol", rol.getName ( ) );
        assertEquals ( "des", rol.getDescription ( ) );
    }

}
