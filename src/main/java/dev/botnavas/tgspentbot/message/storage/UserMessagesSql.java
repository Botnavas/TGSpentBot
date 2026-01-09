package dev.botnavas.tgspentbot.message.storage;

public class UserMessagesSql {
    public static final String FIND_BY_ID = "SELECT * FROM exchange.user_messages " +
            "WHERE message_id = ? AND user_id = ?";
    public static final String CREATE = "INSERT INTO exchange.user_messages" +
            "(message_id, state, sent, sum, date, tag_id, user_id, expense_id) " +
            "VALUES(?, ?, ?, ?, ?, ?, ?, ?)" //+
            //"RETURNING id"
            ;
    public static final String UPDATE = "UPDATE exchange.user_messages " +
            "SET message_id = ?, state = ?, sent = ?, sum = ?, date = ?, tag_id = ?, expense_id = ? " +
            "WHERE message_id = ? AND user_id = ?";
}
