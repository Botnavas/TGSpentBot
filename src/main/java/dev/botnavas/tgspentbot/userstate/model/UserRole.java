package dev.botnavas.tgspentbot.userstate.model;

import java.util.stream.Stream;

public enum UserRole {
    ADMIN("admin"),
    USER("user"),
    SUPPORT("support");
    private final String data;

    UserRole(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return data;
    }

    public static UserRole fromString(String data) {
        return Stream.of(UserRole.values())
                .filter(e -> e.data.equalsIgnoreCase(data))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No enum constant with data value: " + data));
    }
}
