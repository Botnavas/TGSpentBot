package dev.botnavas.tgspentbot.message.storage;

public class UserMessagesSql {
    public static final String FIND_BY_ID = "SELECT * FROM exchange.user_messages " +
            "WHERE message_id = ?";
    public static final String CREATE = "INSERT INTO exchange.user_messages" +
            "(message_id, state, sent, sum, date, tag_id) " +
            "VALUES(?, ?, ?, ?, ?, ?)";
    public static final String UPDATE = "UPDATE exchange.user_messages " +
            "message_id = ?, state = ?, sent = ?, sum = ?, date = ?, tag_id = ? " +
            "WHERE message_id = ?";
}
