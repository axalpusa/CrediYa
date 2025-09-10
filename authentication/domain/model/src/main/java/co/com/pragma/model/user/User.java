package co.com.pragma.model.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class User {
    private UUID idUser;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String address;
    private String documentId;
    private LocalDate birthDate;
    private String phoneNumber;
    private String password;
    private BigDecimal baseSalary;
    private UUID idRol;

    public void merge(User other) {
        if ( other.getFirstName ( ) != null ) this.firstName = other.getFirstName ( );
        if ( other.getLastName ( ) != null ) this.lastName = other.getLastName ( );
        if ( other.getEmailAddress ( ) != null ) this.emailAddress = other.getEmailAddress ( );
        if ( other.getAddress ( ) != null ) this.address = other.getAddress ( );
        if ( other.getDocumentId ( ) != null ) this.documentId = other.getDocumentId ( );
        if ( other.getBirthDate ( ) != null ) this.birthDate = other.getBirthDate ( );
        if ( other.getPhoneNumber ( ) != null ) this.phoneNumber = other.getPhoneNumber ( );
        if ( other.getPassword ( ) != null ) this.password = other.getPassword ( );
        if ( other.getBaseSalary ( ) != null ) this.baseSalary = other.getBaseSalary ( );
        if ( other.getIdRol ( ) != null ) this.idRol = other.getIdRol ( );
    }
}
