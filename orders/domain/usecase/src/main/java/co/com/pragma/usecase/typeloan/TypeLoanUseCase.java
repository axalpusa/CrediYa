package co.com.pragma.usecase.typeloan;

import co.com.pragma.model.typeloan.TypeLoan;
import co.com.pragma.model.typeloan.gateways.TypeLoanRepository;
import exceptions.ValidationException;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class TypeLoanUseCase {

    private final TypeLoanRepository typeLoanRepository;

    public Mono < TypeLoan > saveTypeLoan(TypeLoan typeLoan) {
        return typeLoanRepository.save ( typeLoan );
    }

    public Mono < TypeLoan > updateTypeLoan(TypeLoan typeLoan) {
        return typeLoanRepository.findById ( typeLoan.getIdTypeLoan ( ) )
                .flatMap ( existing -> {
                    if ( typeLoan.getName ( ) != null ) existing.setName ( typeLoan.getName ( ) );
                    if ( typeLoan.getInterestRate ( ) != null )
                        existing.setInterestRate ( typeLoan.getInterestRate ( ) );
                    if ( typeLoan.getAutomaticValidation ( ) != null )
                        existing.setAutomaticValidation ( typeLoan.getAutomaticValidation ( ) );
                    if ( typeLoan.getMinimumAmount ( ) != null )
                        existing.setMinimumAmount ( typeLoan.getMinimumAmount ( ) );
                    if ( typeLoan.getMaximumAmount ( ) != null )
                        existing.setMaximumAmount ( typeLoan.getMaximumAmount ( ) );
                    return typeLoanRepository.save ( existing );
                } );
    }

    public Mono < TypeLoan > getTypeLoanById(UUID id) {
        return typeLoanRepository.findById ( id )
                .switchIfEmpty ( Mono.error ( new ValidationException (
                        List.of ( "Type loan not found: " + id )
                ) ) );
    }

    public Mono < Void > deleteTypeLoanById(UUID id) {
        return typeLoanRepository.deleteById ( id );
    }


    public Flux < TypeLoan > getAllTypesLoan() {
        return typeLoanRepository.findAll ( );
    }

}
