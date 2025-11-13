package dev.botnavas.tgspentbot.callback.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CallbackCommand {
    YESTERDAY("ytd"),
    TODAY("tdy"),
    SELECT_DATE("dat"),
    SELECT_TAG("tag"),
    EDIT("edt"),
    CHANGE_SUM("sum"),
    CHANGE_DATE("cdt"),
    CHANGE_TAG("ctg"),
    DELETE("del"),
    ADD_TAG("addTag"),
    DATE_SELECTED("dateSelected"),
    DELETE_TAG("delTag"),
    MESSAGE_TO_DEL("mtd"),
    UNKNOWN("");

    private final String shortCommand;

    public static CallbackCommand fromShortCommand(String shortCommand) {
        for (CallbackCommand cmd : CallbackCommand.values()) {
            if (cmd.shortCommand.equals(shortCommand)) {
                return cmd;
            }
        }
        return UNKNOWN;
    }
}
