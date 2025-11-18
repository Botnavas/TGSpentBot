package dev.botnavas.tgspentbot.message.model;

import java.util.stream.Stream;

public enum MessageState {
    WAIT_FOR_DATA("wfd"),
    WAIT_FOR_TAG("wft"),
    SAVED_CHANGABLE("scb"),
    UNCHANGABLE("ucb");
    private final String data;
    private MessageState(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return data;
    }

    public static MessageState fromString(String data) {
        return Stream.of(MessageState.values())
                .filter(e -> e.data.equalsIgnoreCase(data))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No enum constant with data value: " + data));
    }
}
