package co.com.pragma.api;

import co.com.pragma.api.dto.request.UserRequestDTO;
import co.com.pragma.api.dto.response.UserResponseDTO;
import co.com.pragma.api.handler.UserHandler;
import co.com.pragma.api.mapper.UserMapperDTO;
import co.com.pragma.api.routerrest.UserRouterRest;
import co.com.pragma.model.user.User;
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
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouterUserHandlerTest {

    private WebTestClient webTestClient;

    private UserUseCase userUseCase;

    private UserMapperDTO userMapper;

    private ObjectMapper objectMapper;

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        userUseCase = mock(UserUseCase.class);
        userMapper = mock(UserMapperDTO.class);
        objectMapper = mock(ObjectMapper.class);
        passwordEncoder = mock(PasswordEncoder.class);
        UserHandler userHandler = new UserHandler(userUseCase, objectMapper, userMapper,passwordEncoder);
        UserRouterRest userRouterRest = new UserRouterRest();

        webTestClient = WebTestClient.bindToRouterFunction(
                userRouterRest.userRouterFunction(userHandler)
        ).build();
    }

    private UserRequestDTO buildRequest() {
        UUID idRolUser = UUID.fromString("a71e243b-e901-4e6e-b521-85ff39ac2f3e");

        UserRequestDTO req = new UserRequestDTO();
        req.setFirstName("axel");
        req.setLastName("Puertas");
        req.setAddress("Av santa rosa");
        req.setEmailAddress("axalpusa11125@gmail.com");
        req.setBirthDate(LocalDate.parse("01-05-1994", DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        req.setDocumentId("48594859");
        req.setPhoneNumber("973157252");
        req.setBaseSalary(new BigDecimal("700000"));
        req.setIdRol(idRolUser);
        return req;
    }

    private User buildModelFromReq(UserRequestDTO req) {
        UUID idRolUser = UUID.fromString("a71e243b-e901-4e6e-b521-85ff39ac2f3e");
        UUID idNewUser = UUID.randomUUID();
        return User.builder()
                .idUser(idNewUser)
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .address(req.getAddress())
                .emailAddress(req.getEmailAddress())
                .birthDate(req.getBirthDate())
                .documentId(req.getDocumentId())
                .phoneNumber(req.getPhoneNumber())
                .baseSalary(req.getBaseSalary())
                .idRol(idRolUser)
                .build();
    }


    @Test
    @DisplayName("POST /api/v1/users - successful")
    void saveUserCorrect() {
        UserRequestDTO req = buildRequest();
        User toSave = buildModelFromReq(req);
        User saved = toSave.toBuilder().build();
        UserResponseDTO response = new UserResponseDTO();
        response.setIdUser(saved.getIdUser());
        response.setFirstName(saved.getFirstName());
        response.setLastName(saved.getLastName());
        response.setAddress(saved.getAddress());
        response.setEmailAddress(saved.getEmailAddress());
        response.setBirthDate(saved.getBirthDate());
        response.setDocumentId(saved.getDocumentId());
        response.setPhoneNumber(saved.getPhoneNumber());
        response.setBaseSalary(saved.getBaseSalary());

        lenient().when(userMapper.toModel(any(UserRequestDTO.class))).thenReturn(toSave);
        lenient().when(userUseCase.saveUser(any(User.class))).thenReturn(Mono.just(saved));
        lenient().when(userMapper.toResponse(any(User.class))).thenReturn(response);

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.emailAddress").isEqualTo(req.getEmailAddress());

    }

    @Test
    @DisplayName("POST /api/v1/users - validation_error")
    void saveUserValidationError() {
        UserRequestDTO req = buildRequest();
        req.setFirstName("");
        User invalidUser = buildModelFromReq(req);
        when(userMapper.toModel(any(UserRequestDTO.class))).thenReturn(invalidUser);
        when(userUseCase.saveUser(any(User.class)))
                .thenReturn(Mono.error(new ValidationException(List.of("First name is required."))));
        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors[0]").isEqualTo("First name is required.");
    }

    @Test
    @DisplayName("POST /api/v1/users - email address duplicate")
    void saveUserExistEmailAddress() {
        UserRequestDTO req = buildRequest();
        User toSave = buildModelFromReq(req);

        when(userMapper.toModel(any(UserRequestDTO.class))).thenReturn(toSave);
        when(userUseCase.saveUser(any(User.class)))
                .thenReturn(Mono.error(new ValidationException(List.of("Email address duplicate."))));

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors[0]").isEqualTo("Email address duplicate.");
    }

}