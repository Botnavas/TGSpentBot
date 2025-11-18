package dev.botnavas.tgspentbot.user.service.model;

import java.util.stream.Stream;

public enum CallbackCommand {
    NEW_TAG("new_tag"),
    TAG_DELETE_MENU("tag_delete_menu"),
    DELETE_TAG("delete_tag"),
    STAT_MENU("stat_menu"),
    BACK("back"),
    UNCHANGABLE("ucb");
    private final String data;
    CallbackCommand(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return data;
    }

    public static CallbackCommand fromString(String data) {
        return Stream.of(CallbackCommand.values())
                .filter(e -> e.data.equalsIgnoreCase(data))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No enum constant with data value: " + data));
    }
}
