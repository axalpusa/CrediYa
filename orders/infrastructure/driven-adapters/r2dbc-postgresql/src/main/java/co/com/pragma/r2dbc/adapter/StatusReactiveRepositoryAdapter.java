package co.com.pragma.r2dbc.adapter;

import co.com.pragma.model.status.Status;
import co.com.pragma.model.status.gateways.StatusRepository;
import co.com.pragma.r2dbc.adapter.interfaces.StatusReactiveRepository;
import co.com.pragma.r2dbc.entities.StatusEntity;
import co.com.pragma.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class StatusReactiveRepositoryAdapter extends ReactiveAdapterOperations <
        Status, StatusEntity, UUID, StatusReactiveRepository
        > implements StatusRepository {
    public StatusReactiveRepositoryAdapter(StatusReactiveRepository repository, ObjectMapper mapper) {
        super ( repository, mapper, d -> mapper.mapBuilder ( d, Status.StatusBuilder.class ).build ( ) );
    }

    @Override
    @Transactional
    public Mono < Status > save(Status status) {
        StatusEntity entity = mapper.map ( status, StatusEntity.class );
        return repository.save ( entity )
                .map ( saved -> mapper.map ( saved, Status.class ) );
    }

    @Override
    public Flux < Status > findAll() {
        return repository.findAll ( )
                .map ( this::toStatus );
    }

    @Override
    public Mono < Status > findById(UUID id) {
        return super.findById ( id );
    }

    @Override
    public Mono < Void > deleteById(UUID id) {
        return repository.deleteById ( id );
    }

    private Status toStatus(StatusEntity entity) {
        return mapper.map ( entity, Status.class );
    }
}
