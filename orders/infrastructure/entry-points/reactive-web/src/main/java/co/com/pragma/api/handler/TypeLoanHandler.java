package co.com.pragma.api.handler;

import co.com.pragma.api.config.ApiPaths;
import co.com.pragma.api.dto.request.TypeLoanRequestDTO;
import co.com.pragma.api.dto.response.TypeLoanResponseDTO;
import co.com.pragma.api.mapper.TypeLoanMapperDTO;
import co.com.pragma.model.typeloan.TypeLoan;
import co.com.pragma.transaction.TransactionalAdapter;
import co.com.pragma.usecase.typeloan.TypeLoanUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import constants.Constants;
import exceptions.NotFoundPragmaException;
import exceptions.ValidationPragmaException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TypeLoanHandler {

    private final TypeLoanUseCase typeloanUseCase;
    private final ObjectMapper objectMapper;
    private final TypeLoanMapperDTO typeLoanMapper;
    private final TransactionalAdapter transactionalAdapter;

    public Mono < ServerResponse > listenSaveTypeLoan(ServerRequest request) {
        return transactionalAdapter.executeInTransaction (
                request.bodyToMono ( TypeLoanRequestDTO.class )
                        .switchIfEmpty ( Mono.error ( new ValidationPragmaException (
                                List.of ( Constants.REQUEST_EMPTY )
                        ) ) )
                        .flatMap ( dto -> Mono.justOrEmpty ( typeLoanMapper.toModel ( dto ) ) )
                        .flatMap ( typeloanUseCase::saveTypeLoan )
                        .flatMap ( savedTypeLoan -> ServerResponse
                                .created ( URI.create ( ApiPaths.TYPELOAN + savedTypeLoan.getIdTypeLoan ( ) ) )
                                .contentType ( MediaType.APPLICATION_JSON )
                                .bodyValue ( savedTypeLoan ) )
        );
    }

    public Mono < ServerResponse > listenUpdateTypeLoan(ServerRequest request) {
        return request.bodyToMono ( TypeLoanResponseDTO.class )
                .flatMap ( dto -> typeloanUseCase.getTypeLoanById ( dto.getIdTypeLoan ( ) )
                        .switchIfEmpty ( Mono.error ( new NotFoundPragmaException ( Constants.TYPE_LOAN_NOT_FOUND  ) ) )
                        .map ( existing -> {
                            TypeLoan partial = objectMapper.convertValue ( dto, TypeLoan.class );
                            if ( partial == null ) partial = new TypeLoan ( );
                            existing.merge ( partial );
                            return existing;
                        } )
                )
                .flatMap ( typeloanUseCase::updateTypeLoan )
                .flatMap ( savedTypeLoan -> ServerResponse.ok ( )
                        .contentType ( MediaType.APPLICATION_JSON )
                        .bodyValue ( savedTypeLoan ) );
    }

    public Mono < ServerResponse > listenGetAllTypesLoan(ServerRequest request) {
        return ServerResponse.ok ( )
                .contentType ( MediaType.TEXT_EVENT_STREAM )
                .body ( typeloanUseCase.getAllTypesLoan ( ), TypeLoanResponseDTO.class );
    }

    public Mono < ServerResponse > listenGetTypeLoanById(ServerRequest request) {
        return Mono.fromCallable ( () -> request.pathVariable ( "idTypeLoan" ) )
                .map ( String::trim )
                .filter ( item -> !item.isBlank ( ) )
                .map ( UUID::fromString )
                .flatMap ( typeloanUseCase::getTypeLoanById )
                .flatMap ( typeLoan -> ServerResponse.ok ( )
                        .contentType ( MediaType.APPLICATION_JSON )
                        .bodyValue ( typeLoan ) )
                .switchIfEmpty ( ServerResponse.notFound ( ).build ( ) );
    }

    public Mono < ServerResponse > listenDeleteTypeLoan(ServerRequest request) {

        return Mono.fromCallable ( () -> request.pathVariable ( "idTypeLoan" ) )
                .map ( String::trim )
                .filter ( item -> !item.isBlank ( ) )
                .map ( UUID::fromString )
                .flatMap ( id -> typeloanUseCase.deleteTypeLoanById ( id )
                        .then ( ServerResponse.noContent ( ).build ( ) )
                )
                .switchIfEmpty ( ServerResponse.notFound ( ).build ( ) );
    }

}
