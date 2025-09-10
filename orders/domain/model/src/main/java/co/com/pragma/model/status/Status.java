package co.com.pragma.model.status;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Status {
    private UUID idStatus;
    private String name;
    private String description;

    public void merge(Status other) {
        if ( other.getName ( ) != null ) this.name = other.getName ( );
        if ( other.getDescription ( ) != null ) this.description = other.getDescription ( );
    }
}
