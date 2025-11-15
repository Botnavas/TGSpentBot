package dev.botnavas.tgspentbot;

import dev.botnavas.tgspentbot.config.AppConfig;
import dev.botnavas.tgspentbot.storage.model.impl.PGConnection;
import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

@Log4j2
public class Main {
    public static void main(String[] args) {
        AppConfig.loadConfig("application.properties");
        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            var connection = new PGConnection();
            botsApplication.registerBot(AppConfig.getBotToken(), new BotService(connection));
            Thread.currentThread().join();
        } catch (Exception e) {
            log.error(String.format("Error in main: %s", e.getMessage()));
            System.exit(-1);
        }
    }
}