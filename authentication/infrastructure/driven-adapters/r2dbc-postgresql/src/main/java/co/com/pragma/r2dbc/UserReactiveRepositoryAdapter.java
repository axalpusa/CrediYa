package co.com.pragma.r2dbc;

import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
import co.com.pragma.r2dbc.entities.UserEntity;
import co.com.pragma.r2dbc.helper.ReactiveAdapterOperations;
import co.com.pragma.r2dbc.interfaces.UserReactiveRepository;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class UserReactiveRepositoryAdapter extends ReactiveAdapterOperations <
        User, UserEntity, UUID, UserReactiveRepository
        > implements UserRepository {
    public UserReactiveRepositoryAdapter(UserReactiveRepository repository, ObjectMapper mapper) {
        super ( repository, mapper, d -> mapper.mapBuilder ( d, User.UserBuilder.class ).build ( ) );
    }

    @Override
    public Mono < User > save(User user) {
        UserEntity entity = mapper.map ( user, UserEntity.class );
        return repository.save ( entity )
                .map ( saved -> mapper.map ( saved, User.class ) );
    }

    @Override
    public Mono < User > findById(UUID id) {
        return super.findById ( id );
    }

    @Override
    public Mono < User > findByEmailAddress(String email) {
        return repository.findByEmailAddress ( email )
                .map ( entity -> mapper.map ( entity, User.class ) );
    }

    @Override
    public Mono < Void > deleteById(UUID id) {
        return repository.deleteById ( id );
    }

    @Override
    public Flux < User > findAll() {
        return repository.findAll ( )
                .map ( this::toUser );
    }

    private User toUser(UserEntity entity) {
        return mapper.map ( entity, User.class );
    }
}
