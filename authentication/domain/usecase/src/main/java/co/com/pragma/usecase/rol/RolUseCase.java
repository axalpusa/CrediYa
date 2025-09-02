package co.com.pragma.usecase.rol;

import co.com.pragma.model.rol.Rol;
import co.com.pragma.model.rol.gateways.RolRepository;
import exceptions.ValidationException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class RolUseCase {

    private final RolRepository rolRepository;

    public Mono < Rol > saveRol(Rol rol) {
        return rolRepository.save ( rol );
    }

    public Mono < Rol > updateRol(Rol rol) {
        return rolRepository.findById ( rol.getIdRol ( ) )
                .flatMap ( existingRol -> {
                    if ( rol.getName ( ) != null ) existingRol.setName ( rol.getName ( ) );
                    if ( rol.getDescription ( ) != null ) existingRol.setDescription ( rol.getDescription ( ) );
                    return rolRepository.save ( existingRol );
                } );
    }

    public Mono < Rol > getRolById(UUID id) {
        return rolRepository.findById ( id )
                .switchIfEmpty ( Mono.error ( new ValidationException (
                        List.of ( "Rol not found: " + id )
                ) ) );
    }

    public Mono < Void > deleteRolById(UUID id) {
        return rolRepository.deleteById ( id );
    }

    public Flux < Rol > getAllRol() {
        return rolRepository.findAll ( );
    }
}
