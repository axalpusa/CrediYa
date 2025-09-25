package co.com.pragma.usecase.user;

import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
import exceptions.ValidationPragmaException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static constants.Constants.ADDRESS_REQUIRED;
import static constants.Constants.BASE_SALARY_REQUIRED;
import static constants.Constants.DOCUMENT_REQUIRED;
import static constants.Constants.EMAIL_DUPLICATED;
import static constants.Constants.EMAIL_REQUIRED;
import static constants.Constants.FIRST_NAME_REQUIRED;
import static constants.Constants.LAST_NAME_REQUIRED;
import static constants.Constants.USER_NOT_FOUND;

@RequiredArgsConstructor
public class UserUseCase {
    private final UserRepository userRepository;

    public Mono < User > saveUser(User user) {
        return validateUser ( user )
                .flatMap ( this::saveNewUser )
                ;
    }

    private Mono < User > validateUser(User user) {
        List < String > errors = new ArrayList <> ( );

        if ( isBlank ( user.getFirstName ( ) ) ) errors.add ( FIRST_NAME_REQUIRED );
        if ( isBlank ( user.getLastName ( ) ) ) errors.add ( LAST_NAME_REQUIRED );
        if ( isBlank ( user.getEmailAddress ( ) ) ) errors.add ( EMAIL_REQUIRED );
        if ( isBlank ( user.getAddress ( ) ) ) errors.add ( ADDRESS_REQUIRED );
        if ( isBlank ( user.getDocumentId ( ) ) ) errors.add ( DOCUMENT_REQUIRED );
        if ( user.getBaseSalary ( ) == null ||
                user.getBaseSalary ( ).doubleValue ( ) < 0 ||
                user.getBaseSalary ( ).doubleValue ( ) > 15_000_000 ) {
            errors.add ( BASE_SALARY_REQUIRED );
        }
        if ( !errors.isEmpty ( ) ) return Mono.error ( new ValidationPragmaException ( errors ) );

        return userRepository.findByEmailAddress ( user.getEmailAddress ( ) )
                .flatMap ( existingUser ->
                        Mono. < User >error ( new ValidationPragmaException (
                                List.of ( EMAIL_DUPLICATED )
                        ) )
                )
                .switchIfEmpty ( Mono.just ( user ) );
    }

    public boolean isBlank(String value) {
        return value == null || value.trim ( ).isEmpty ( );
    }

    private Mono < User > saveNewUser(User user) {
        return userRepository.save ( user );
    }

    public Mono < User > updateUser(User user) {
        return userRepository.findById ( user.getIdUser ( ) )
                .flatMap ( existingUser -> {
                    if ( user.getFirstName ( ) != null ) existingUser.setFirstName ( user.getFirstName ( ) );
                    if ( user.getLastName ( ) != null ) existingUser.setLastName ( user.getLastName ( ) );
                    if ( user.getEmailAddress ( ) != null ) existingUser.setEmailAddress ( user.getEmailAddress ( ) );
                    if ( user.getAddress ( ) != null ) existingUser.setAddress ( user.getAddress ( ) );
                    if ( user.getDocumentId ( ) != null ) existingUser.setDocumentId ( user.getDocumentId ( ) );
                    if ( user.getBirthDate ( ) != null ) existingUser.setBirthDate ( user.getBirthDate ( ) );
                    if ( user.getPhoneNumber ( ) != null ) existingUser.setPhoneNumber ( user.getPhoneNumber ( ) );
                    if ( user.getBaseSalary ( ) != null ) existingUser.setBaseSalary ( user.getBaseSalary ( ) );
                    if ( user.getIdRol ( ) != null ) existingUser.setIdRol ( user.getIdRol ( ) );
                    return userRepository.save ( existingUser );
                } );
    }

    public Mono < User > getUserById(UUID id) {
        return userRepository.findById ( id )
                .switchIfEmpty ( Mono.error ( new ValidationPragmaException (
                        List.of ( USER_NOT_FOUND + id )
                ) ) );
    }

    public Mono < Void > deleteUserById(UUID id) {
        return userRepository.deleteById ( id );
    }

    public Flux < User > getAllUsers() {
        return userRepository.findAll ( );
    }

    public Mono < User > findByEmailAddress(String emailAddress) {
        return userRepository.findByEmailAddress ( emailAddress );
    }

}
