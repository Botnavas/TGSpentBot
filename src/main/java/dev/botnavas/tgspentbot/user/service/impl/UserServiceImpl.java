package dev.botnavas.tgspentbot.user.service.impl;

import dev.botnavas.tgspentbot.config.exception.DoubleTagNameException;
import dev.botnavas.tgspentbot.storage.model.DBConnection;
import dev.botnavas.tgspentbot.tag.model.Tag;
import dev.botnavas.tgspentbot.tag.storage.PGTagStorage;
import dev.botnavas.tgspentbot.tag.storage.TagStorage;
import dev.botnavas.tgspentbot.user.model.User;
import dev.botnavas.tgspentbot.user.service.UserService;
import dev.botnavas.tgspentbot.user.service.model.CallbackCommand;
import dev.botnavas.tgspentbot.user.storage.PGUserStorage;
import dev.botnavas.tgspentbot.user.storage.UserStorage;
import dev.botnavas.tgspentbot.userstate.model.UserRole;
import dev.botnavas.tgspentbot.userstate.model.UserState;
import dev.botnavas.tgspentbot.userstate.model.UserStates;
import dev.botnavas.tgspentbot.userstate.storage.PGUserStateStorage;
import dev.botnavas.tgspentbot.userstate.storage.UserStateStorage;
import dev.botnavas.tgspentbot.utils.KeyboardUtils;
import dev.botnavas.tgspentbot.utils.MessageUtils;
import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Optional;

@Log4j2
public class UserServiceImpl implements UserService {
    private final TelegramClient telegramClient;
    private final UserStorage userStorage;
    private  final UserStateStorage userStateStorage;
    private final TagStorage tagStorage;

    public UserServiceImpl(TelegramClient telegramClient, DBConnection connection) {
        this.telegramClient = telegramClient;
        userStorage = new PGUserStorage(connection);
        userStateStorage = new PGUserStateStorage(connection);
        tagStorage = new PGTagStorage(connection);
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
        sendMainMenu(user);
    }

    @Override
    public void sendMainMenu(User user) {
        var mainMenuMessage = MessageUtils.createHtml("%s, выберите действие:", user.getFirstName());
        sendInlineMessage(user, mainMenuMessage, KeyboardUtils.createMainMenu());
        UserState state = getState(user).setDeafult();
        var update =  userStateStorage.update(state);
        if (update.isEmpty()) {
            userStateStorage.create(state);
        }
    }

    @Override
    public Optional<User> findByChatId(long chatId) {
        return userStorage.findByChatId(chatId);
    }

    @Override
    public void handleNewTagCommand(User user) {
        UserState state = getState(user);
        state.setState(UserStates.NEW_TAG);
        var messageText = MessageUtils.createHtml("Введите новый тег");
        sendTextMessage(user, messageText);
        userStateStorage.update(state);
    }

    public void register(User user) {
        userStorage.createUser(user);
        UserState userState = UserState.builder()
                .userId(user.getId())
                .state(UserStates.DEFAULT)
                .botMessageId(0)
                .role(UserRole.USER)
                .build();
        userStateStorage.create(userState);
    }

    public UserState getState(User user)
    {
       var state =  userStateStorage.findByUserId(user.getId());
       return state.orElseGet(() -> UserState.builder()
               .userId(user.getId())
               .role(UserRole.USER)
               .build().setDeafult());
    }

    @Override
    public void addNewTag(String tagName, User user) {
        Tag tag = Tag.builder()
                .name(tagName)
                .userId(user.getId())
                .build();
        try {
            var result = tagStorage.create(tag);
            String messageText;
            if (result.isEmpty()) {
                messageText = MessageUtils.createHtml("Тег ",
                        tagName, " не был добавлен");
            } else {
                messageText = MessageUtils.createHtml("Тег ",
                        tagName, " успешно добавлен");
            }
            sendTextMessage(user, messageText);
        } catch (DoubleTagNameException d) {
            String messageText = MessageUtils.createHtml("Тег с таким названием уже существует");
            sendTextMessage(user, messageText);
        }

        UserState state = getState(user);
        userStateStorage.update(state.setDeafult());
    }

    @Override
    public void handleDeleteTagCommand(User user, int tagId, int messageId) {
        Optional<Tag> optionalTag = tagStorage.findById(tagId);
        if (optionalTag.isEmpty()) {
            String messageText = MessageUtils.createHtml("Этот тег был уже удален");
            sendTextMessage(user, messageText);
            handleTagDeleteMenuCommand(user, messageId);
            return;
        }

        boolean deleted = tagStorage.delete(optionalTag.get());
        if (deleted) {
            String messageText = MessageUtils.createHtml("Тег", optionalTag.get().getName(), "успешно удален");
            sendTextMessage(user, messageText);
            //deleteMessage(messageId, user);
            handleTagDeleteMenuCommand(user, messageId);
        } else {
            String messageText = MessageUtils.createHtml("Тег", optionalTag.get().getName(), "не был удален");
            sendTextMessage(user, messageText);
            //deleteMessage(messageId, user);
            handleTagDeleteMenuCommand(user, messageId);
        }
    }

    @Override
    public void handleTagDeleteMenuCommand(User user, int messageId) {
        InlineKeyboardMarkup markup = KeyboardUtils.createTagKeyboard(user, tagStorage, CallbackCommand.DELETE_TAG);
        var messageText = MessageUtils.createHtml("Выберите тег для удаления:");
        editMessage(user, messageText, markup, messageId);
    }

    public void deleteMessage(int messageId, User user) {
        DeleteMessage deleteMessage = DeleteMessage.builder()
                .messageId(messageId)
                .chatId(user.getChatId())
                .build();
        try {
            telegramClient.execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.error(String.format("Exception while deleting message with id:\n%s\nError message: %s", messageId, e.getMessage()));
        }
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

    public void sendInlineMessage(User user, String text, InlineKeyboardMarkup markup) {
        var message = SendMessage.builder()
                .chatId(user.getChatId())
                .text(text)
                .parseMode(ParseMode.HTML)
                .replyMarkup(markup)
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error(String.format("Exception while sending message:\n%s\nMessage: %s", message.toString(), e.getMessage()));
        }
    }

    public void editMessage(User user, String text, InlineKeyboardMarkup markup, int messageId)
    {
        var editMessage = EditMessageText.builder()
                .messageId(messageId)
                .text(text)
                .replyMarkup(markup)
                .chatId(user.getChatId())
                .build();
        try {
            telegramClient.execute(editMessage);
        } catch (TelegramApiException e) {
            log.error(String.format("Exception while editing message with id:\n%s\nError message: %s", messageId, e.getMessage()));
            sendInlineMessage(user, text, markup);
        }
    }
}
