package dev.botnavas.tgspentbot.user.storage;

public class UserSql {
    public static final String FIND_BY_ID = "SELECT * " +
            "FROM exchange.users " +
            "WHERE id = ?";

    public static final String CREATE_USER = "INSERT INTO " +
            "exchange.users(id, chat_id, username, first_name, second_name, last_interaction_dttm) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
}
