package enums;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum StatusEnum {
    PENDENT ( UUID.fromString ( "f8820448-a6ef-4d0d-beb8-130a71dc3fda" ) ),
    APPROVED ( UUID.fromString ( "1603cbb9-f4ad-4112-9804-c3d4c04a48f5" ) ),
    REVISION ( UUID.fromString ( "f7820448-a6ef-4d0d-beb8-130a71dc3fda" ) ),
    REJECTED ( UUID.fromString ( "723b63d8-507e-48a3-a8f1-896df273dfc8" ) );
    private final UUID id;

    private static final Map < UUID, StatusEnum > ID_MAP =
            Stream.of ( values ( ) ).collect ( Collectors.toMap ( StatusEnum::getId, e -> e ) );

    StatusEnum(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public static StatusEnum fromId(UUID id) {
        StatusEnum status = ID_MAP.get ( id );
        if ( status == null ) {
            throw new IllegalArgumentException ( "No StatusEnum found with id: " + id );
        }
        return status;
    }

    public static UUID getIdFromName(String name) {
        try {
            return StatusEnum.valueOf ( name ).getId ( );
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException ( "No StatusEnum found with name: " + name, e );
        }
    }
}
