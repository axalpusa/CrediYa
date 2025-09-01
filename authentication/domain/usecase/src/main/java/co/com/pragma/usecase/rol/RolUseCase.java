package co.com.pragma.usecase.rol;

import co.com.pragma.model.rol.Rol;
import co.com.pragma.model.rol.gateways.RolRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public class RolUseCase {

    private final RolRepository rolRepository;

    public Mono<Rol> saveRol(Rol rol) {
        return rolRepository.save(rol);
    }

    public Mono<Rol> updateRol(Rol rol) {
        return rolRepository.save(rol);
    }

    public Mono<Rol> getRolById(UUID id) {
        return rolRepository.findById(id);
    }

    public Mono<Void> deleteRolById(UUID id) {
        return rolRepository.deleteById(id);
    }

    public Flux<Rol> getAllRol() {
        return rolRepository.findAll();
    }
}
