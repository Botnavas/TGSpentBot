package dev.botnavas.tgspentbot.userstate.model;

import java.util.stream.Stream;

public enum UserStates {
    NEW_TAG("new_tag"),
    DELETE_TAG("delete_tag"),
    STAT_MENU("stat_menu"),
    WAIT_FOR_DATA_INPUT("data"),
    CHANGE_SUM("change_sum"),
    DEFAULT("d");
    private final String data;

    UserStates(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return data;
    }

    public static UserStates fromString(String data) {
        return Stream.of(UserStates.values())
                .filter(e -> e.data.equalsIgnoreCase(data))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No enum constant with data value: " + data));
    }
}
