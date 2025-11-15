package dev.botnavas.tgspentbot.tag.storage;

public class TagSql {
    public static final String FIND_BY_ID = "SELECT * FROM exchange.tags " +
            "WHERE id = ?";
    public static final String FIND_USER_TAGS_BY_USER_ID = "SELECT * FROM exchange.tags " +
            "WHERE user_id = ?";
    public static final String CREATE = "INSERT INTO exchange.tags" +
            "(user_id, name) " +
            "VALUES(?, ?)" +
            "RETURNING id";
    public static final String DELETE = "DELETE FROM exchange.tags " +
            "WHERE id = ?";
}
