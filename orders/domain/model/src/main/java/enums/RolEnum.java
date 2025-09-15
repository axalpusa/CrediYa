package enums;

import java.util.UUID;

public enum RolEnum {
    ADMIN(UUID.fromString("facbe723-85f2-4f5a-92d6-a4a4a3a5b8ca")),
    ASSESSOR(UUID.fromString("beaed8b3-7090-4c58-a3d5-7578ce4f1b6a")),
    REJECTED(UUID.fromString("723b63d8-507e-48a3-a8f1-896df273dfc8")),
    CLIENT(UUID.fromString("a71e243b-e901-4e6e-b521-85ff39ac2f3e"));
    private final UUID id;

    RolEnum(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
