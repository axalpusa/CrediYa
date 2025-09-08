package co.com.pragma.r2dbc;

import co.com.pragma.model.user.User;
import co.com.pragma.r2dbc.entities.UserEntity;
import co.com.pragma.r2dbc.interfaces.UserReactiveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.Mockito.*;

class UserReactiveRepositoryAdapterTest {

    private UserReactiveRepository repository;
    private ObjectMapper mapper;
    private UserReactiveRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock(UserReactiveRepository.class);
        mapper = mock(ObjectMapper.class);
        adapter = new UserReactiveRepositoryAdapter(repository, mapper);
    }

    @Test
    void shouldSaveUser() {
        User user = buildUser();
        UserEntity entity = buildUserEntity();

        when(mapper.map(user, UserEntity.class)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(Mono.just(entity));
        when(mapper.map(entity, User.class)).thenReturn(user);

        StepVerifier.create(adapter.save(user))
                .expectNext(user)
                .verifyComplete();

        verify(repository).save(entity);
        verify(mapper).map(user, UserEntity.class);
        verify(mapper).map(entity, User.class);
    }

    @Test
    void shouldFindByEmail() {
        String email = "test@example.com";
        User user = buildUser();
        UserEntity entity = buildUserEntity();

        when(repository.findByEmailAddress(email)).thenReturn(Mono.just(entity));
        when(mapper.map(entity, User.class)).thenReturn(user);

        StepVerifier.create(adapter.findByEmailAddress(email))
                .expectNext(user)
                .verifyComplete();

        verify(repository).findByEmailAddress(email);
        verify(mapper).map(entity, User.class);
    }

    @Test
    void shouldDeleteById() {
        UUID id = UUID.randomUUID();
        when(repository.deleteById(id)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.deleteById(id))
                .verifyComplete();

        verify(repository).deleteById(id);
    }

    @Test
    void shouldFindAllUsers() {
        User user = buildUser();
        UserEntity entity = buildUserEntity();

        when(repository.findAll()).thenReturn(Flux.just(entity));
        when(mapper.map(entity, User.class)).thenReturn(user);

        StepVerifier.create(adapter.findAll())
                .expectNext(user)
                .verifyComplete();

        verify(repository).findAll();
        verify(mapper).map(entity, User.class);
    }

    private User buildUser() {
        return User.builder()
                .idUser(UUID.randomUUID())
                .firstName("Axel")
                .lastName("Puertas")
                .emailAddress("axelp@example.com")
                .address("Av Santa Rosa")
                .documentId("48295730")
                .birthDate(LocalDate.of(1994, 5, 1))
                .phoneNumber("973157252")
                .baseSalary(new BigDecimal("700000"))
                .build();
    }

    private UserEntity buildUserEntity() {
        UserEntity entity = new UserEntity();
        entity.setIdUser(UUID.randomUUID());
        entity.setFirstName("Axel");
        entity.setLastName("Puertas");
        entity.setEmailAddress("axelp@example.com");
        entity.setAddress("Av Santa Rosa");
        entity.setDocumentId("48295730");
        entity.setBirthDate(LocalDate.of(1994, 5, 1));
        entity.setPhoneNumber("973157252");
        entity.setBaseSalary(new BigDecimal("700000"));
        return entity;
    }
}
