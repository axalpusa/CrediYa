package co.com.pragma.api.enums;

import java.util.UUID;

public enum RolEnum {
    ADMIN(UUID.fromString("facbe723-85f2-4f5a-92d6-a4a4a3a5b8ca")),
    ASSESSOR(UUID.fromString("beaed8b3-7090-4c58-a3d5-7578ce4f1b6a")),
    CLIENT(UUID.fromString("a71e243b-e901-4e6e-b521-85ff39ac2f3e"));
    private final UUID id;

    RolEnum(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
