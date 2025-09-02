package co.com.pragma.usecase.status;

import co.com.pragma.model.status.Status;
import co.com.pragma.model.status.gateways.StatusRepository;
import exceptions.ValidationException;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class StatusUseCase {

    private final StatusRepository statusRepository;

    public Mono < Status > saveStatus(Status status) {
        return statusRepository.save ( status );
    }

    public Mono < Status > updateStatus(Status status) {
        return statusRepository.findById ( status.getIdStatus ( ) )
                .flatMap ( existing -> {
                    if ( status.getName ( ) != null ) existing.setName ( status.getName ( ) );
                    if ( status.getDescription ( ) != null )
                        existing.setDescription ( status.getDescription ( ) );
                    return statusRepository.save ( existing );
                } );
    }

    public Mono < Status > getStatusById(UUID id) {
        return statusRepository.findById ( id )
                .switchIfEmpty ( Mono.error ( new ValidationException (
                        List.of ( "Status not found: " + id )
                ) ) );
    }

    public Mono < Void > deleteStatusById(UUID id) {
        return statusRepository.deleteById ( id );
    }


    public Flux < Status > getAlStatus() {
        return statusRepository.findAll ( );
    }

}
