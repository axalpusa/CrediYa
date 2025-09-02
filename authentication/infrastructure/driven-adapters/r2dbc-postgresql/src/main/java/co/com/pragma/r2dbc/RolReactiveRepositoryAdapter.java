package co.com.pragma.r2dbc;

import co.com.pragma.model.rol.Rol;
import co.com.pragma.model.rol.gateways.RolRepository;
import co.com.pragma.r2dbc.entities.RolEntity;
import co.com.pragma.r2dbc.helper.ReactiveAdapterOperations;
import co.com.pragma.r2dbc.interfaces.RolReactiveRepository;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class RolReactiveRepositoryAdapter extends ReactiveAdapterOperations <
        Rol, RolEntity, UUID, RolReactiveRepository
        > implements RolRepository {
    public RolReactiveRepositoryAdapter(RolReactiveRepository repository, ObjectMapper mapper) {
        super ( repository, mapper, d -> mapper.mapBuilder ( d, Rol.RolBuilder.class ).build ( ) );
    }

    @Override
    @Transactional
    public Mono < Rol > save(Rol rol) {
        RolEntity entity = mapper.map ( rol, RolEntity.class );
        return repository.save ( entity )
                .map ( saved -> mapper.map ( saved, Rol.class ) );
    }

    @Override
    public Mono < Rol > findById(UUID id) {
        return super.findById ( id );
    }


    @Override
    public Mono < Void > deleteById(UUID id) {
        return repository.deleteById ( id );
    }

    @Override
    public Flux < Rol > findAll() {
        return repository.findAll ( )
                .map ( this::toRol );
    }

    private Rol toRol(RolEntity entity) {
        return mapper.map ( entity, Rol.class );
    }
}
