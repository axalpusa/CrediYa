package co.com.pragma.model.typeloan.gateways;

import co.com.pragma.model.typeloan.TypeLoan;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TypeLoanRepository {

    Flux < TypeLoan > findAll();

    Mono < TypeLoan > save(TypeLoan typeLoan);

    Mono < TypeLoan > findById(UUID id);

    Mono < Void > deleteById(UUID id);

}
