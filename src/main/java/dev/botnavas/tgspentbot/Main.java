package dev.botnavas.tgspentbot;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import dev.botnavas.tgspentbot.utilites.AppConfig;

public class Main {
    public static void main(String[] args) {
         final String botToken = AppConfig.getBotToken();
        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(botToken, new BotClass(botToken));
            System.out.println("MyAmazingBot successfully started!");
            // Ensure this prcess wait forever
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}