package dev.botnavas.tgspentbot;

import dev.botnavas.tgspentbot.config.AppConfig;
import dev.botnavas.tgspentbot.storage.model.DBConnection;
import dev.botnavas.tgspentbot.user.mapper.UserMapper;
import dev.botnavas.tgspentbot.user.model.User;
import dev.botnavas.tgspentbot.user.service.UserService;
import dev.botnavas.tgspentbot.user.service.impl.UserServiceImpl;
import dev.botnavas.tgspentbot.user.service.model.CallbackCommand;
import dev.botnavas.tgspentbot.userstate.model.UserState;
import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Optional;

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
            handleCommand(messageText, user);
            return;
        }

        handleMessage(messageText, user);
    }

    public void handleMessage(String messageText, dev.botnavas.tgspentbot.user.model.User user) {
        UserState state = userService.getState(user);
        switch (state.getState()) {
            case NEW_TAG -> {
                userService.addNewTag(messageText, user);
                return;
            }
        }
    }

    public void handleCommand(String messageText, dev.botnavas.tgspentbot.user.model.User user) {
        switch (messageText) {
            case "/start" -> {
                userService.sendMainMenu(user);
            }
        }
    }

    public void handleCallbackQuery(Update update) {
        var query = update.getCallbackQuery();
        var userOptional = UserMapper.fromCallbackQuery(query, userService);
        if (userOptional.isEmpty()) {
            log.error(String.format("User not found from CallbackQuery %s", query));
            return;
        }

        var user = userOptional.get();
        var command = query.getData();
        int messageId = query.getMessage().getMessageId();
        String[] commandData = command.split(";");
        CallbackCommand callbackCommand;
        try {
            callbackCommand = CallbackCommand.fromString(commandData[0]);
        } catch (IllegalArgumentException e) {
            log.error(String.format("Incorrect callback command: %s", command));
            return;
        }
        switch (callbackCommand) {
            case NEW_TAG -> {
                userService.handleNewTagCommand(user);
            }
            case TAG_DELETE_MENU -> {
                userService.handleTagDeleteMenuCommand(user, messageId);
            }
            case DELETE_TAG -> {
                userService.handleDeleteTagCommand(user,
                        Integer.parseInt(commandData[1]), messageId);
            }
            case STAT_MENU -> {
            }
        }

        AnswerCallbackQuery answer = new AnswerCallbackQuery(query.getId());
        try {
            telegramClient.execute(answer);
        } catch (TelegramApiException e) {
            log.error(String.format("error answering callback query %s", e.getMessage()));
        }
    }
}
