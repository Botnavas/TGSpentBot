package dev.botnavas.tgspentbot;

import dev.botnavas.tgspentbot.config.AppConfig;
import dev.botnavas.tgspentbot.storage.model.DBConnection;
import dev.botnavas.tgspentbot.user.mapper.UserMapper;
import dev.botnavas.tgspentbot.user.service.UserService;
import dev.botnavas.tgspentbot.user.service.impl.UserServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Log4j2
public class BotService implements LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient;

    private final UserService userService;

    public BotService(DBConnection connection) {
        telegramClient = new OkHttpTelegramClient(AppConfig.getBotToken());
        userService = new UserServiceImpl(telegramClient, connection);
    }

    @Override
    public void consume(Update update) {
        if (update.hasCallbackQuery()) {
            handleCallbackQuery(update);
        }
        if (!update.hasMessage()) {
            return;
        }
        var msg = update.getMessage();
        var user = UserMapper.fromTelegram(msg);

        if (!userService.isRegistered(user)) {
            userService.sendWelcomeMessage(user);
            return;
        }

        var messageText = msg.getText();

        if (messageText.startsWith("/")) {
            handleCommand(messageText);
            return;
        }

        handleMessage(messageText);
    }

    public void handleMessage(String messageText) {

    }

    public void handleCommand(String messageText) {

    }

    public void handleCallbackQuery(Update update) {

    }
}
