package co.com.pragma.model.rol;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Rol {
    private UUID idRol;
    private String name;
    private String description;
    public void merge(Rol other) {
        if (other.getName() != null) this.name = other.getName();
        if (other.getDescription() != null) this.description = other.getDescription();
    }
}
