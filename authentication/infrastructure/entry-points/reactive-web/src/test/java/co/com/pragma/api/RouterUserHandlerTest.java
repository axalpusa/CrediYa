package co.com.pragma.api;

import co.com.pragma.api.config.ApiPaths;
import co.com.pragma.api.config.GlobalErrorHandler;
import co.com.pragma.api.dto.request.UserRequestDTO;
import co.com.pragma.api.dto.response.RolResponseDTO;
import co.com.pragma.api.dto.response.UserResponseDTO;
import co.com.pragma.api.handler.UserHandler;
import co.com.pragma.api.mapper.UserMapperDTO;
import co.com.pragma.api.routerrest.UserRouterRest;
import co.com.pragma.model.rol.Rol;
import co.com.pragma.model.user.User;
import co.com.pragma.transaction.TransactionalAdapter;
import co.com.pragma.usecase.user.UserUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import exceptions.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouterUserHandlerTest {

    private WebTestClient webTestClient;

    private UserUseCase userUseCase;

    private UserMapperDTO userMapper;

    private ObjectMapper objectMapper;

    private PasswordEncoder passwordEncoder;

    private TransactionalAdapter transactionalAdapter;

    @BeforeEach
    void setup() {
        userUseCase = mock ( UserUseCase.class );
        userMapper = mock ( UserMapperDTO.class );
        objectMapper = mock ( ObjectMapper.class );
        passwordEncoder = mock ( PasswordEncoder.class );
        transactionalAdapter = mock ( TransactionalAdapter.class );
        objectMapper = new ObjectMapper ( );
        UserHandler userHandler = new UserHandler ( userUseCase, objectMapper, userMapper, passwordEncoder, transactionalAdapter );
        UserRouterRest userRouterRest = new UserRouterRest ( );
        GlobalErrorHandler globalErrorHandler = new GlobalErrorHandler ( objectMapper );
        webTestClient = WebTestClient
                .bindToRouterFunction ( userRouterRest.userRouterFunction ( userHandler )
                ).handlerStrategies ( HandlerStrategies.builder ( )
                        .exceptionHandler ( globalErrorHandler )
                        .build ( ) )
                .build ( );
    }

    private UserRequestDTO buildRequest() {
        UUID idRolUser = UUID.fromString ( "a71e243b-e901-4e6e-b521-85ff39ac2f3e" );

        UserRequestDTO req = new UserRequestDTO ( );
        req.setFirstName ( "axel" );
        req.setLastName ( "Puertas" );
        req.setAddress ( "Av santa rosa" );
        req.setEmailAddress ( "axalpusa11125@gmail.com" );
        req.setBirthDate ( LocalDate.parse ( "01-05-1994", DateTimeFormatter.ofPattern ( "dd-MM-yyyy" ) ) );
        req.setDocumentId ( "48594859" );
        req.setPhoneNumber ( "973157252" );
        req.setBaseSalary ( new BigDecimal ( "700000" ) );
        req.setIdRol ( idRolUser );
        return req;
    }

    private User buildModelFromReq(UserRequestDTO req) {
        UUID idRolUser = UUID.fromString ( "a71e243b-e901-4e6e-b521-85ff39ac2f3e" );
        UUID idNewUser = UUID.randomUUID ( );
        return User.builder ( )
                .idUser ( idNewUser )
                .firstName ( req.getFirstName ( ) )
                .lastName ( req.getLastName ( ) )
                .address ( req.getAddress ( ) )
                .emailAddress ( req.getEmailAddress ( ) )
                .birthDate ( req.getBirthDate ( ) )
                .documentId ( req.getDocumentId ( ) )
                .phoneNumber ( req.getPhoneNumber ( ) )
                .baseSalary ( req.getBaseSalary ( ) )
                .idRol ( idRolUser )
                .build ( );
    }


    @Test
    @DisplayName("POST /api/v1/users - successful")
    void saveUserCorrect() {
        UserRequestDTO req = buildRequest ( );
        User toSave = buildModelFromReq ( req );
        User saved = toSave.toBuilder ( ).build ( );
        UserResponseDTO response = new UserResponseDTO ( );
        response.setIdUser ( saved.getIdUser ( ) );
        response.setFirstName ( saved.getFirstName ( ) );
        response.setLastName ( saved.getLastName ( ) );
        response.setAddress ( saved.getAddress ( ) );
        response.setEmailAddress ( saved.getEmailAddress ( ) );
        response.setBirthDate ( saved.getBirthDate ( ) );
        response.setDocumentId ( saved.getDocumentId ( ) );
        response.setPhoneNumber ( saved.getPhoneNumber ( ) );
        response.setBaseSalary ( saved.getBaseSalary ( ) );

        lenient ( ).when ( userMapper.toModel ( any ( UserRequestDTO.class ) ) ).thenReturn ( toSave );
        lenient ( ).when ( userUseCase.saveUser ( any ( User.class ) ) ).thenReturn ( Mono.just ( saved ) );
        lenient ( ).when ( userMapper.toResponse ( any ( User.class ) ) ).thenReturn ( response );
        lenient ( ).when ( transactionalAdapter.executeInTransaction ( any ( Mono.class ) ) )
                .thenAnswer ( invocation -> invocation. < Mono < ? > >getArgument ( 0 ) );


        webTestClient.post ( )
                .uri ( ApiPaths.USERS )
                .contentType ( MediaType.APPLICATION_JSON )
                .bodyValue ( req )
                .exchange ( )
                .expectStatus ( ).isCreated ( )
                .expectBody ( )
                .jsonPath ( "$.emailAddress" ).isEqualTo ( req.getEmailAddress ( ) );

    }

    @Test
    @DisplayName("POST /api/v1/users - validation_error")
    void saveUserValidationError() {
        UserRequestDTO req = buildRequest ( );
        req.setFirstName ( "" );
        User invalidUser = buildModelFromReq ( req );
        when ( userMapper.toModel ( any ( UserRequestDTO.class ) ) ).thenReturn ( invalidUser );
        when ( userUseCase.saveUser ( any ( User.class ) ) )
                .thenReturn ( Mono.error ( new ValidationException ( List.of ( "First name is required." ) ) ) );
        when ( transactionalAdapter.executeInTransaction ( any ( Mono.class ) ) )
                .thenAnswer ( invocation -> invocation. < Mono < ? > >getArgument ( 0 ) );
        webTestClient.post ( )
                .uri ( ApiPaths.USERS )
                .contentType ( MediaType.APPLICATION_JSON )
                .bodyValue ( req )
                .exchange ( )
                .expectStatus ( ).isBadRequest ( )
                .expectBody ( )
                .jsonPath ( "$.details" ).isEqualTo ( "First name is required." );
    }

    @Test
    @DisplayName("POST /api/v1/users - email address duplicate")
    void saveUserExistEmailAddress() {
        UserRequestDTO req = buildRequest ( );
        User toSave = buildModelFromReq ( req );

        when ( userMapper.toModel ( any ( UserRequestDTO.class ) ) ).thenReturn ( toSave );
        when ( userUseCase.saveUser ( any ( User.class ) ) )
                .thenReturn ( Mono.error ( new ValidationException ( List.of ( "Email address duplicate." ) ) ) );
        when ( transactionalAdapter.executeInTransaction ( any ( Mono.class ) ) )
                .thenAnswer ( invocation -> invocation. < Mono < ? > >getArgument ( 0 ) );
        webTestClient.post ( )
                .uri ( ApiPaths.USERS )
                .contentType ( MediaType.APPLICATION_JSON )
                .bodyValue ( req )
                .exchange ( )
                .expectStatus ( ).isBadRequest ( )
                .expectBody ( )
                .jsonPath ( "$.details" ).isEqualTo ( "Email address duplicate." );
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} - found")
    void getUserById() {
        UserRequestDTO req = buildRequest ( );
        User model = buildModelFromReq ( req );

        when ( userUseCase.getUserById ( model.getIdUser ( ) ) )
                .thenReturn ( Mono.just ( model ) );

        webTestClient.get ( )
                .uri ( "/api/v1/users/{idUser}", model.getIdUser ( ) )
                .exchange ( )
                .expectStatus ( ).isOk ( )
                .expectBody ( )
                .jsonPath ( "$.idUser" ).isEqualTo ( model.getIdUser ( ).toString ( ) )
                .jsonPath ( "$.firstName" ).isEqualTo ( model.getFirstName ( ) );
    }

    @Test
    @DisplayName("GET /api/v1/users/email/{email} - found")
    void getUserByEmail() {
        UserRequestDTO req = buildRequest ( );
        User model = buildModelFromReq ( req );

        when ( userUseCase.findByEmailAddress ( model.getEmailAddress ( ) ) )
                .thenReturn ( Mono.just ( model ) );

        webTestClient.get ( )
                .uri ( "/api/v1/users/byEmail/{email}", model.getEmailAddress ( ) )
                .exchange ( )
                .expectStatus ( ).isOk ( )
                .expectBody ( )
                .jsonPath ( "$.emailAddress" ).isEqualTo ( model.getEmailAddress ( ) );
    }

    @Test
    void testUpdateUser() {
        UUID uuid = UUID.randomUUID ( );
        UUID idRolUser = UUID.fromString ( "a71e243b-e901-4e6e-b521-85ff39ac2f3e" );
        UserResponseDTO dto = new UserResponseDTO ( );
        dto.setIdUser ( uuid );
        dto.setFirstName ( "Nuevo nombre" );

        User existingUser = new User ( );
        existingUser.setIdUser (uuid);
        existingUser.setFirstName ( "axel" );
        existingUser.setLastName ( "Puertas" );
        existingUser.setAddress ( "Av santa rosa" );
        existingUser.setEmailAddress ( "axalpusa11125@gmail.com" );
        existingUser.setBirthDate ( LocalDate.parse ( "01-05-1994", DateTimeFormatter.ofPattern ( "dd-MM-yyyy" ) ) );
        existingUser.setDocumentId ( "48594859" );
        existingUser.setPhoneNumber ( "973157252" );
        existingUser.setBaseSalary ( new BigDecimal ( "700000" ) );
        existingUser.setPassword ( "$2a$10$mfILaHia4jqInB2mUQ2Vt.0PJxjJoXODUnkzchdHH6hxzPoF6xSjO" );
        existingUser.setIdRol ( idRolUser );

        User updatedUser = new User();
        updatedUser.setIdUser (uuid);
        updatedUser.setFirstName ( "alexandre" );
        updatedUser.setLastName ( "Puertas" );
        updatedUser.setAddress ( "Av santa rosa" );
        updatedUser.setEmailAddress ( "axalpusa11125@gmail.com" );
        updatedUser.setBirthDate ( LocalDate.parse ( "01-05-1994", DateTimeFormatter.ofPattern ( "dd-MM-yyyy" ) ) );
        updatedUser.setDocumentId ( "48594859" );
        updatedUser.setPhoneNumber ( "973157252" );
        updatedUser.setBaseSalary ( new BigDecimal ( "700000" ) );
        updatedUser.setPassword ( "$2a$10$mfILaHia4jqInB2mUQ2Vt.0PJxjJoXODUnkzchdHH6hxzPoF6xSjO" );
        updatedUser.setIdRol ( idRolUser );

        when(userUseCase.getUserById (uuid)).thenReturn(Mono.just(existingUser));
        when(userUseCase.updateUser (any(User.class))).thenReturn(Mono.just(updatedUser));

        webTestClient.put ( )
                .uri ( "/api/v1/users" )
                .contentType ( MediaType.APPLICATION_JSON )
                .bodyValue ( dto )
                .exchange ( )
                .expectStatus ( ).isOk ( )
                .expectBody ( )
                .jsonPath ( "$.idUser" ).isEqualTo ( dto.getIdUser ( ).toString ( ) )
                .jsonPath ( "$.firstName" ).isEqualTo ( "alexandre" );
    }

    @Test
    void testDeleteUserSuccess() {
        UUID userId = UUID.randomUUID ( );

        when ( userUseCase.deleteUserById ( userId ) ).thenReturn ( Mono.empty ( ) );

        webTestClient.delete ( )
                .uri ( "/api/v1/users/{idUser}", userId )
                .exchange ( )
                .expectStatus ( ).isNoContent ( );
    }

    @Test
    void testDeleteUserEmptyId() {
        webTestClient.delete ( )
                .uri ( "/api/v1/users/{idUser}", "" )
                .exchange ( )
                .expectStatus ( ).isNotFound ( );
    }

    @Test
    @DisplayName("GET /api/v1/users/all - listen all users SSE")
    void listenGetAllUsers() {
        User user1 = new User();
        user1.setIdUser(UUID.randomUUID());
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setEmailAddress("john@example.com");
        user1.setBaseSalary(BigDecimal.valueOf(5000));

        User user2 = new User();
        user2.setIdUser(UUID.randomUUID());
        user2.setFirstName("Jane");
        user2.setLastName("Doe");
        user2.setEmailAddress("jane@example.com");
        user2.setBaseSalary(BigDecimal.valueOf(6000));

        when(userUseCase.getAllUsers()).thenReturn(Flux.just(user1, user2));

        webTestClient.get()
                .uri("/api/v1/users/all")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectBodyList(UserResponseDTO.class)
                .hasSize(2)
                .value(users -> {
                    assert users.get(0).getFirstName().equals("John");
                    assert users.get(1).getFirstName().equals("Jane");
                });

        verify(userUseCase, times(1)).getAllUsers();
    }

}