package dev.botnavas.tgspentbot.storage.model;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface DBConnection {
    PreparedStatement prepare(String sqlText) throws SQLException;
}
