package dev.botnavas.tgspentbot.user.service.impl;

import dev.botnavas.tgspentbot.storage.model.DBConnection;
import dev.botnavas.tgspentbot.user.model.User;
import dev.botnavas.tgspentbot.user.service.UserService;
import dev.botnavas.tgspentbot.user.storage.PGUserStorage;
import dev.botnavas.tgspentbot.user.storage.UserStorage;
import dev.botnavas.tgspentbot.utils.MessageUtils;
import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Log4j2
public class UserServiceImpl implements UserService {
    private final TelegramClient telegramClient;
    private final UserStorage userStorage;

    public UserServiceImpl(TelegramClient telegramClient, DBConnection connection) {
        this.telegramClient = telegramClient;
        userStorage = new PGUserStorage(connection);
    }

    @Override
    public boolean isRegistered(User user) {
        return userStorage.findById(user.getId()).isPresent();
    }

    @Override
    public void sendWelcomeMessage(User user) {
        register(user);

        var welcomeMessage = MessageUtils.createHtml("Hello, %s!\nWelcome to bot", user.getFirstName());
        sendTextMessage(user, welcomeMessage);
    }

    public void register(User user) {
        userStorage.createUser(user);
    }

    public void sendTextMessage(User user, String text) {
        var message = SendMessage.builder()
                .chatId(user.getChatId())
                .text(text)
                .parseMode(ParseMode.HTML)
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error(String.format("Exception while sending message:\n%s\nMessage: %s", message.toString(), e.getMessage()));
        }
    }
}
