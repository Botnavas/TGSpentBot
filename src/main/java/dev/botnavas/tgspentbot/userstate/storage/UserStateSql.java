package dev.botnavas.tgspentbot.userstate.storage;

import dev.botnavas.tgspentbot.userstate.model.UserStates;

public class UserStateSql {
    public static final String FIND_BY_ID = "SELECT * FROM exchange.user_state " +
            "WHERE user_id = ?";
    public static final String CREATE = "INSERT INTO exchange.user_state" +
            "(user_id, state, bot_message_id, role) " +
            "VALUES(?, ?, ?, ?)";
    public static final String UPDATE = "UPDATE exchange.user_state " +
        "SET state = ?, bot_message_id = ?, role = ? " +
        "WHERE user_id = ?";

    public static final String SET_DEFAULT = "UPDATE exchange.user_state " +
            "SET state = '" + UserStates.DEFAULT + "', bot_message_id = 0 " +
            "WHERE user_id = ?";
}
