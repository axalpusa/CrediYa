package co.pragma;

import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
import co.com.pragma.usecase.user.UserUseCase;
import exceptions.ValidationPragmaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static constants.Constants.ADDRESS_REQUIRED;
import static constants.Constants.BASE_SALARY_REQUIRED;
import static constants.Constants.DOCUMENT_REQUIRED;
import static constants.Constants.EMAIL_DUPLICATED;
import static constants.Constants.EMAIL_REQUIRED;
import static constants.Constants.FIRST_NAME_REQUIRED;
import static constants.Constants.LAST_NAME_REQUIRED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserTest {
    private UserRepository userRepository;
    private UserUseCase userUseCase;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock ( UserRepository.class );
        userUseCase = new UserUseCase ( userRepository );
    }

    @Test
    void shouldReturnUserWhenExists() {
        UUID idRolUser = UUID.fromString ( "a71e243b-e901-4e6e-b521-85ff39ac2f3e" );
        UUID id = UUID.randomUUID ( );
        User user = new User ( );
        user.setFirstName ( "axel" );
        user.setLastName ( "Puertas" );
        user.setAddress ( "Av santa rosa" );
        user.setEmailAddress ( "axalpusa11125@gmail.com" );
        user.setBirthDate ( LocalDate.parse ( "01-05-1994", DateTimeFormatter.ofPattern ( "dd-MM-yyyy" ) ) );
        user.setDocumentId ( "48594859" );
        user.setPhoneNumber ( "973157252" );
        user.setBaseSalary ( new BigDecimal ( "700000" ) );
        user.setPassword ( "$2a$10$mfILaHia4jqInB2mUQ2Vt.0PJxjJoXODUnkzchdHH6hxzPoF6xSjO" );
        user.setIdRol ( idRolUser );

        when ( userRepository.findById ( id ) ).thenReturn ( Mono.just ( user ) );

        StepVerifier.create ( userUseCase.getUserById ( id ) )
                .expectNextMatches ( u -> u.getFirstName ( ).equals ( "axel" ) &&
                        u.getIdRol ( ).equals ( idRolUser ) &&
                        u.getEmailAddress ( ).equals ( "axalpusa11125@gmail.com" ) )
                .verifyComplete ( );

        verify ( userRepository ).findById ( id );
    }


    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        UUID id = UUID.randomUUID ( );

        when ( userRepository.findById ( id ) ).thenReturn ( Mono.empty ( ) );

        StepVerifier.create ( userUseCase.getUserById ( id ) )
                .expectErrorMatches ( throwable -> throwable instanceof ValidationPragmaException &&
                        throwable.getMessage ( ).contains ( id.toString ( ) ) )
                .verify ( );

        verify ( userRepository ).findById ( id );
    }

    @Test
    void shouldSaveUserSuccessfully() {
        User user = new User ( );
        user.setFirstName ( "Axel" );
        user.setLastName ( "Puertas" );
        user.setEmailAddress ( "axel@example.com" );
        user.setAddress ( "Piura" );
        user.setDocumentId ( "12345678" );
        user.setBaseSalary ( new BigDecimal ( "5000" ) );

        when ( userRepository.findByEmailAddress ( user.getEmailAddress ( ) ) ).thenReturn ( Mono.empty ( ) );
        when ( userRepository.save ( user ) ).thenReturn ( Mono.just ( user ) );

        StepVerifier.create ( userUseCase.saveUser ( user ) )
                .expectNextMatches ( u -> u.getEmailAddress ( ).equals ( "axel@example.com" ) &&
                        u.getFirstName ( ).equals ( "Axel" ) )
                .verifyComplete ( );

        verify ( userRepository ).findByEmailAddress ( user.getEmailAddress ( ) );
        verify ( userRepository ).save ( user );
    }

    @Test
    void shouldThrowValidationExceptionForDuplicateEmail() {
        User user = new User ( );
        user.setFirstName ( "Axel" );
        user.setLastName ( "Puertas" );
        user.setEmailAddress ( "axel@example.com" );
        user.setAddress ( "Piura" );
        user.setDocumentId ( "12345678" );
        user.setBaseSalary ( new BigDecimal ( "5000" ) );

        when ( userRepository.findByEmailAddress ( user.getEmailAddress ( ) ) ).thenReturn ( Mono.just ( user ) );

        StepVerifier.create ( userUseCase.saveUser ( user ) )
                .expectErrorSatisfies ( e -> {
                    assert e instanceof ValidationPragmaException;
                    assert ((ValidationPragmaException) e).getErrors ( ).contains ( EMAIL_DUPLICATED );
                } )
                .verify ( );

        verify ( userRepository ).findByEmailAddress ( user.getEmailAddress ( ) );
    }


    @Test
    void shouldUpdateUserSuccessfully() {
        UUID id = UUID.randomUUID ( );
        User existing = new User ( );
        existing.setIdUser ( id );
        existing.setFirstName ( "Axel" );

        User updated = new User ( );
        updated.setIdUser ( id );
        updated.setFirstName ( "Alex" );

        when ( userRepository.findById ( id ) ).thenReturn ( Mono.just ( existing ) );
        when ( userRepository.save ( existing ) ).thenReturn ( Mono.just ( updated ) );

        StepVerifier.create ( userUseCase.updateUser ( updated ) )
                .expectNextMatches ( u -> u.getFirstName ( ).equals ( "Alex" ) )
                .verifyComplete ( );

        verify ( userRepository ).findById ( id );
        verify ( userRepository ).save ( existing );
    }

    @Test
    void shouldDeleteUserSuccessfully() {
        UUID id = UUID.randomUUID ( );

        when ( userRepository.deleteById ( id ) ).thenReturn ( Mono.empty ( ) );

        StepVerifier.create ( userUseCase.deleteUserById ( id ) )
                .verifyComplete ( );

        verify ( userRepository ).deleteById ( id );
    }

    @Test
    void shouldGetAllUsersSuccessfully() {
        User user = new User ( );
        user.setFirstName ( "Axel" );

        when ( userRepository.findAll ( ) ).thenReturn ( reactor.core.publisher.Flux.just ( user ) );

        StepVerifier.create ( userUseCase.getAllUsers ( ) )
                .expectNextMatches ( u -> u.getFirstName ( ).equals ( "Axel" ) )
                .verifyComplete ( );

        verify ( userRepository ).findAll ( );
    }

    @Test
    void findByEmailAddress_shouldReturnUser_whenUserExists() {
        String email = "test@example.com";
        User user = new User ( );
        user.setIdUser ( UUID.randomUUID ( ) );
        user.setEmailAddress ( email );

        when ( userRepository.findByEmailAddress ( email ) ).thenReturn ( Mono.just ( user ) );

        StepVerifier.create ( userUseCase.findByEmailAddress ( email ) )
                .expectNextMatches ( u -> u.getEmailAddress ( ).equals ( email ) )
                .verifyComplete ( );

        verify ( userRepository, times ( 1 ) ).findByEmailAddress ( email );
    }

    @Test
    void findByEmailAddress_shouldReturnEmpty_whenUserDoesNotExist() {
        String email = "noexist@example.com";

        when ( userRepository.findByEmailAddress ( email ) ).thenReturn ( Mono.empty ( ) );

        StepVerifier.create ( userUseCase.findByEmailAddress ( email ) )
                .expectNextCount ( 0 )
                .verifyComplete ( );

        verify ( userRepository, times ( 1 ) ).findByEmailAddress ( email );
    }

    @Test
    void saveUser_shouldFail_whenBaseSalaryIsInvalid() {
        User user = new User ( );
        user.setFirstName ( "Axel" );
        user.setLastName ( "Puertas" );
        user.setEmailAddress ( "axel@example.com" );
        user.setAddress ( "Some address" );
        user.setDocumentId ( "12345678" );
        user.setBaseSalary ( BigDecimal.valueOf ( 20_000_000 ) );

        StepVerifier.create ( userUseCase.saveUser ( user ) )
                .expectErrorSatisfies ( e -> {
                    assert e instanceof ValidationPragmaException;
                    assert ((ValidationPragmaException) e).getErrors ( )
                            .contains ( BASE_SALARY_REQUIRED );
                } )
                .verify ( );

        verify ( userRepository, times ( 0 ) ).save ( any ( ) );
    }

    @Test
    void saveUser_shouldFail_whenBaseSalaryNegative() {
        User user = new User ( );
        user.setFirstName ( "Axel" );
        user.setLastName ( "Puertas" );
        user.setEmailAddress ( "axel@example.com" );
        user.setAddress ( "Some address" );
        user.setDocumentId ( "12345678" );
        user.setBaseSalary ( BigDecimal.valueOf ( -1000 ) );

        StepVerifier.create ( userUseCase.saveUser ( user ) )
                .expectErrorSatisfies ( e -> {
                    assert e instanceof ValidationPragmaException;
                    assert ((ValidationPragmaException) e).getErrors ( )
                            .contains ( BASE_SALARY_REQUIRED );
                } )
                .verify ( );

        verify ( userRepository, times ( 0 ) ).save ( any ( ) );
    }

    @Test
    void saveUser_shouldFail_FirstName() {
        User user = new User ( );
        user.setFirstName ( "" );
        user.setLastName ( "Puertas" );
        user.setEmailAddress ( "axel@example.com" );
        user.setAddress ( "Some address" );
        user.setDocumentId ( "12345678" );
        user.setBaseSalary ( BigDecimal.valueOf ( 1500 ) );

        StepVerifier.create ( userUseCase.saveUser ( user ) )
                .expectErrorSatisfies ( e -> {
                    assert e instanceof ValidationPragmaException;
                    assert ((ValidationPragmaException) e).getErrors ( )
                            .contains ( FIRST_NAME_REQUIRED );
                } )
                .verify ( );

        verify ( userRepository, times ( 0 ) ).save ( any ( ) );
    }

    @Test
    void saveUser_shouldFail_LastName() {
        User user = new User ( );
        user.setFirstName ( "axel" );
        user.setLastName ( "" );
        user.setEmailAddress ( "axel@example.com" );
        user.setAddress ( "Some address" );
        user.setDocumentId ( "12345678" );
        user.setBaseSalary ( BigDecimal.valueOf ( 1500 ) );

        StepVerifier.create ( userUseCase.saveUser ( user ) )
                .expectErrorSatisfies ( e -> {
                    assert e instanceof ValidationPragmaException;
                    assert ((ValidationPragmaException) e).getErrors ( )
                            .contains ( LAST_NAME_REQUIRED );
                } )
                .verify ( );

        verify ( userRepository, times ( 0 ) ).save ( any ( ) );
    }

    @Test
    void saveUser_shouldFail_EmailAddress() {
        User user = new User ( );
        user.setFirstName ( "axel" );
        user.setLastName ( "puertas" );
        user.setEmailAddress ( "" );
        user.setAddress ( "Some address" );
        user.setDocumentId ( "12345678" );
        user.setBaseSalary ( BigDecimal.valueOf ( 1500 ) );

        StepVerifier.create ( userUseCase.saveUser ( user ) )
                .expectErrorSatisfies ( e -> {
                    assert e instanceof ValidationPragmaException;
                    assert ((ValidationPragmaException) e).getErrors ( )
                            .contains ( EMAIL_REQUIRED );
                } )
                .verify ( );

        verify ( userRepository, times ( 0 ) ).save ( any ( ) );
    }

    @Test
    void saveUser_shouldFail_Address() {
        User user = new User ( );
        user.setFirstName ( "axel" );
        user.setLastName ( "puertas" );
        user.setEmailAddress ( "axel@example.com" );
        user.setAddress ( "" );
        user.setDocumentId ( "12345678" );
        user.setBaseSalary ( BigDecimal.valueOf ( 1500 ) );

        StepVerifier.create ( userUseCase.saveUser ( user ) )
                .expectErrorSatisfies ( e -> {
                    assert e instanceof ValidationPragmaException;
                    assert ((ValidationPragmaException) e).getErrors ( )
                            .contains ( ADDRESS_REQUIRED );
                } )
                .verify ( );

        verify ( userRepository, times ( 0 ) ).save ( any ( ) );
    }

    @Test
    void saveUser_shouldFail_document() {
        User user = new User ( );
        user.setFirstName ( "axel" );
        user.setLastName ( "puertas" );
        user.setEmailAddress ( "axel@example.com" );
        user.setAddress ( "asd" );
        user.setDocumentId ( "" );
        user.setBaseSalary ( BigDecimal.valueOf ( 1500 ) );

        StepVerifier.create ( userUseCase.saveUser ( user ) )
                .expectErrorSatisfies ( e -> {
                    assert e instanceof ValidationPragmaException;
                    assert ((ValidationPragmaException) e).getErrors ( )
                            .contains ( DOCUMENT_REQUIRED );
                } )
                .verify ( );

        verify ( userRepository, times ( 0 ) ).save ( any ( ) );
    }

    @Test
    void saveUser_shouldFail_BaseSalaryNull() {
        User user = new User ( );
        user.setFirstName ( "axel" );
        user.setLastName ( "puertas" );
        user.setEmailAddress ( "axel@example.com" );
        user.setAddress ( "asd" );
        user.setDocumentId ( "23132123" );
        user.setBaseSalary ( null );

        StepVerifier.create ( userUseCase.saveUser ( user ) )
                .expectErrorSatisfies ( e -> {
                    assert e instanceof ValidationPragmaException;
                    assert ((ValidationPragmaException) e).getErrors ( )
                            .contains ( BASE_SALARY_REQUIRED );
                } )
                .verify ( );

        verify ( userRepository, times ( 0 ) ).save ( any ( ) );
    }

    @Test
    void updateUser_shouldUpdateOnlyNonNullFields() {
        UUID userId = UUID.randomUUID ( );
        UUID rolId = UUID.randomUUID ( );

        User existingUser = new User ( );
        existingUser.setIdUser ( userId );
        existingUser.setLastName ( "OldLast" );
        existingUser.setEmailAddress ( "old@example.com" );
        existingUser.setAddress ( "OldAddress" );
        existingUser.setDocumentId ( "OldDoc" );
        existingUser.setIdRol ( UUID.randomUUID ( ) );

        User updatedUser = new User ( );
        updatedUser.setIdUser ( userId );
        updatedUser.setFirstName ( "NewFirst" );
        updatedUser.setLastName ( "NewLast" );
        updatedUser.setEmailAddress ( "new@example.com" );
        updatedUser.setAddress ( "NewAddress" );
        updatedUser.setDocumentId ( "NewDoc" );
        updatedUser.setBirthDate ( LocalDate.of ( 1994, 5, 1 ) );
        updatedUser.setPhoneNumber ( "NewPhoneNumber" );
        updatedUser.setIdRol ( rolId );
        existingUser.setBaseSalary ( BigDecimal.valueOf ( 2000.0 ) );

        when ( userRepository.findById ( userId ) ).thenReturn ( Mono.just ( existingUser ) );
        when ( userRepository.save ( any ( User.class ) ) )
                .thenAnswer ( invocation -> Mono.just ( invocation.getArgument ( 0 ) ) );

        StepVerifier.create ( userUseCase.updateUser ( updatedUser ) )
                .consumeNextWith ( user -> {
                    assertEquals ( "NewFirst", user.getFirstName ( ) );
                    assertEquals ( "NewLast", user.getLastName ( ) );
                    assertEquals ( "new@example.com", user.getEmailAddress ( ) );
                    assertEquals ( "NewAddress", user.getAddress ( ) );
                    assertEquals ( "NewDoc", user.getDocumentId ( ) );
                    assertEquals ( "NewPhoneNumber", user.getPhoneNumber ( ) );
                    assertEquals ( LocalDate.of ( 1994, 5, 1 ), user.getBirthDate ( ) );
                    assertEquals ( rolId, user.getIdRol ( ) );
                    assertEquals ( 2000.0, user.getBaseSalary ( ).doubleValue ( ) );

                } )
                .verifyComplete ( );

        verify ( userRepository ).save ( any ( User.class ) );
    }

    @Test
    void isBlank_shouldReturnTrueForNullOrEmpty() {
        UserUseCase useCase = new UserUseCase ( userRepository );

        assertTrue ( useCase.isBlank ( null ) );
        assertTrue ( useCase.isBlank ( "" ) );
        assertTrue ( useCase.isBlank ( "   " ) );
        assertFalse ( useCase.isBlank ( "Axel" ) );
    }

    @Test
    void merge_shouldUpdateFields_whenOtherHasValues() {
        UUID userId = UUID.randomUUID ( );
        UUID roleId = UUID.randomUUID ( );
        User original = new User ( );
        original.setIdUser ( userId );
        original.setFirstName ( "Axel" );
        original.setLastName ( "Puertas" );
        original.setEmailAddress ( "old@example.com" );
        original.setAddress ( "Old address" );
        original.setDocumentId ( "12345678" );
        original.setBirthDate ( LocalDate.of ( 1990, 1, 1 ) );
        original.setPhoneNumber ( "999999999" );
        original.setPassword ( "oldPass" );
        original.setBaseSalary ( BigDecimal.valueOf ( 5000 ) );
        original.setIdRol ( roleId );

        User other = new User ( );
        other.setFirstName ( "Alex" );

        original.merge ( other );

        assertThat ( original.getFirstName ( ) ).isEqualTo ( "Alex" );
        assertThat ( original.getIdUser ( ) ).isEqualTo ( userId );
    }

    @Test
    void merge_shouldNotUpdate_whenOtherHasNullFields() {
        UUID userId = UUID.randomUUID ( );
        User original = new User ( );
        original.setIdUser ( userId );
        original.setFirstName ( "Axel" );

        User other = new User ( );

        original.merge ( other );

        assertThat ( original.getFirstName ( ) ).isEqualTo ( "Axel" );
        assertThat ( original.getIdUser ( ) ).isEqualTo ( userId );
    }

    @Test
    void merge_shouldUpdateOnlyNonNullFields() {
        UUID role1 = UUID.randomUUID();
        UUID role2 = UUID.randomUUID();

        User original = User.builder()
                .firstName("Axel")
                .lastName("Puertas")
                .emailAddress("axel@example.com")
                .address("Piura")
                .documentId("48295730")
                .birthDate(LocalDate.of(1995, 1, 1))
                .phoneNumber("973157252")
                .password("1234")
                .baseSalary(new BigDecimal("5000"))
                .idRol(role1)
                .build();

        User updates = User.builder()
                .firstName("Alexandre")
                .emailAddress("alexandre@example.com")
                .baseSalary(new BigDecimal("6000"))
                .idRol(role2)
                .build();

        original.merge(updates);

        assertEquals("Alexandre", original.getFirstName());
        assertEquals("Alexandre@example.com".toLowerCase(), original.getEmailAddress().toLowerCase());
        assertEquals(new BigDecimal("6000"), original.getBaseSalary());
        assertEquals(role2, original.getIdRol());
    }

    @Test
    void merge_shouldNotChangeOriginalIfOtherIsEmpty() {
        User original = User.builder()
                .firstName("Axel")
                .build();

        User empty = new User();
        original.merge(empty);

        assertEquals("Axel", original.getFirstName());
    }
    @Test
    void shouldMergeAllNonNullFields() {
        User original = new User();
        original.setFirstName("Axel");
        original.setLastName("Puertas");
        original.setEmailAddress("axel@old.com");
        original.setAddress("Old address");
        original.setDocumentId("123");
        original.setBirthDate(LocalDate.of(1995, 1, 1));
        original.setPhoneNumber("111111111");
        original.setPassword("oldPass");

        User updates = new User();
        updates.setFirstName("Alexandre");
        updates.setEmailAddress("axel@new.com");
        updates.setAddress("New address");
        updates.setDocumentId("456");
        updates.setBirthDate(LocalDate.of(2000, 12, 25));
        updates.setPhoneNumber("999999999");
        updates.setPassword("newPass");

        original.merge(updates);

        assertEquals("Alexandre", original.getFirstName());
        assertEquals("Puertas", original.getLastName());
        assertEquals("axel@new.com", original.getEmailAddress());
        assertEquals("New address", original.getAddress());
        assertEquals("456", original.getDocumentId());
        assertEquals(LocalDate.of(2000, 12, 25), original.getBirthDate());
        assertEquals("999999999", original.getPhoneNumber());
        assertEquals("newPass", original.getPassword());
    }

}
