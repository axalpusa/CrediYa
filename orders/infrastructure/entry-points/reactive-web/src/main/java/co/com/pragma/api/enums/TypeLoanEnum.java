package co.com.pragma.api.enums;

import java.util.UUID;

public enum TypeLoanEnum {
    TYPE1 ( UUID.fromString ( "f7820448-a6ef-4d0d-beb8-130a71dc3fd4" ) ),
    TYPE2 ( UUID.fromString ( "f8820448-a6ef-4d0d-beb8-130a71dc3fd0" ) );
    private final UUID id;

    TypeLoanEnum(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
