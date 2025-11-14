package dev.botnavas.tgspentbot.storage.impl;

import dev.botnavas.tgspentbot.utilites.AppConfig;
import lombok.extern.log4j.Log4j2;

import java.sql.*;

@Log4j2
public class PGConnection {
    private final Connection connection;

    public PGConnection() throws SQLException {
        connection = DriverManager.getConnection(
                AppConfig.getDbUrl(),
                AppConfig.getDbUser(),
                AppConfig.getDbPass());
        log.info(String.format("Connected to DB"));
    }

    public PreparedStatement prepare(String sqlText) throws SQLException {
        return connection.prepareStatement(sqlText);
    }
}

