package co.com.pragma.api.enums;

import java.util.UUID;

public enum StatusEnum {
    PENDENT ( UUID.fromString ( "f8820448-a6ef-4d0d-beb8-130a71dc3fda" ) ),
    APPROVED (UUID.fromString ( "1603cbb9-f4ad-4112-9804-c3d4c04a48f5" )),
    REVISION ( UUID.fromString ( "f7820448-a6ef-4d0d-beb8-130a71dc3fda" ) );
    private final UUID id;

    StatusEnum(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
    }
