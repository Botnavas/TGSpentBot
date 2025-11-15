package dev.botnavas.tgspentbot.storage.model.impl;

import dev.botnavas.tgspentbot.config.AppConfig;
import dev.botnavas.tgspentbot.storage.model.DBConnection;
import lombok.extern.log4j.Log4j2;

import java.sql.*;

@Log4j2
public class PGConnection implements DBConnection {
    private final Connection connection;

    public PGConnection() throws SQLException {
        connection = DriverManager.getConnection(
                AppConfig.getDbUrl(),
                AppConfig.getDbUser(),
                AppConfig.getDbPass());
        log.info("Connected to DB");
    }

    public PreparedStatement prepare(String sqlText) throws SQLException {
        return connection.prepareStatement(sqlText);
    }
}

