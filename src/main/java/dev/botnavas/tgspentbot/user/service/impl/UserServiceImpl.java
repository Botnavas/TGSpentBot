package dev.botnavas.tgspentbot.user.service.impl;

import dev.botnavas.tgspentbot.config.exception.DoubleTagNameException;
import dev.botnavas.tgspentbot.expense.model.Expense;
import dev.botnavas.tgspentbot.expense.storage.ExpenseStorage;
import dev.botnavas.tgspentbot.expense.storage.PGExpenseStorage;
import dev.botnavas.tgspentbot.message.model.MessageState;
import dev.botnavas.tgspentbot.message.model.UserMessage;
import dev.botnavas.tgspentbot.message.storage.PGUserMessageStorage;
import dev.botnavas.tgspentbot.message.storage.UserMessageStorage;
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
import dev.botnavas.tgspentbot.utils.InputDetector;
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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.util.Optional;

@Log4j2
public class UserServiceImpl implements UserService {
    private final TelegramClient telegramClient;
    private final UserStorage userStorage;
    private final UserStateStorage userStateStorage;
    private final TagStorage tagStorage;
    private final UserMessageStorage userMessageStorage;
    private final ExpenseStorage expenseStorage;
    private final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendPattern("dd.MM.yyyy")
            .parseDefaulting(ChronoField.ERA, 1) // 1 = н.э. (AD)
            .toFormatter()
            .withResolverStyle(ResolverStyle.STRICT);

    public UserServiceImpl(TelegramClient telegramClient, DBConnection connection) {
        this.telegramClient = telegramClient;
        userStorage = new PGUserStorage(connection);
        userStateStorage = new PGUserStateStorage(connection);
        tagStorage = new PGTagStorage(connection);
        userMessageStorage = new PGUserMessageStorage(connection);
        expenseStorage = new PGExpenseStorage(connection);
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
        var update = userStateStorage.update(state);
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

    public UserState getState(User user) {
        var state = userStateStorage.findByUserId(user.getId());
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
                messageText = MessageUtils.createHtml("Тег %s%s",
                        tagName, " не был добавлен");
            } else {
                messageText = MessageUtils.createHtml("Тег %s%s",
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
        var optionalTag = tagStorage.findById(tagId);
        if (optionalTag.isEmpty()) {
            String messageText = MessageUtils.createHtml("Этот тег был уже удален");
            sendTextMessage(user, messageText);
            handleTagDeleteMenuCommand(user, messageId);
            return;
        }

        boolean deleted = tagStorage.delete(optionalTag.get());
        if (deleted) {
            String messageText = MessageUtils.createHtml("Тег %s%s",
                    optionalTag.get().getName(), " успешно удален");
            sendTextMessage(user, messageText);
            //deleteMessage(messageId, user);
            handleTagDeleteMenuCommand(user, messageId);
        } else {
            String messageText = MessageUtils.createHtml("Тег %s%s",
                    optionalTag.get().getName(), "не был удален");
            sendTextMessage(user, messageText);
            //deleteMessage(messageId, user);
            handleTagDeleteMenuCommand(user, messageId);
        }
    }

    @Override
    public void handleTagDeleteMenuCommand(User user, int messageId) {
        InlineKeyboardMarkup markup = KeyboardUtils.createTagKeyboard(user, tagStorage, CallbackCommand.DELETE_TAG);
        var messageText = MessageUtils.createHtml("Выберите тег для удаления:");
        editInlineMessage(user, messageText, markup, messageId);
    }

    @Override
    public void handleDefaultMessage(String messageText, User user) {
        var type = InputDetector.getInputType(messageText);
        if (!type.equals(InputDetector.InputType.Number)) {
            sendTextMessage(user, MessageUtils.createHtml("ведите сумму"));
        }

        int sum = (int) Math.ceil(Double.parseDouble(messageText));
        String message = MessageUtils.createTextSum(sum, true);
        var keyboard = KeyboardUtils.createSumKeyboard(MessageState.WAIT_FOR_DATA, user, tagStorage);

        var id = sendInlineMessage(user, message, keyboard);
        UserMessage userMessage = UserMessage.builder()
                .messageId(id)
                .sum(sum)
                .sent(LocalDateTime.now())
                .userId(user.getId())
                .state(MessageState.WAIT_FOR_DATA)
                .build();
        userMessageStorage.create(userMessage);
    }

    @Override
    public void handleDateOnButtonCommand(User user, int messageId, CallbackCommand command) {
        var optionalUserMessage = userMessageStorage.findById(messageId, user.getId());
        if (optionalUserMessage.isEmpty()) {
            editInlineMessage(user, "Данные отсутствуют. Возврат в главное меню.",
                    KeyboardUtils.createMainMenu(), messageId);
            return;
        }

        var userMessage = optionalUserMessage.get();
        LocalDate date = command.equals(CallbackCommand.TODAY) ?
                LocalDate.now() : LocalDate.now().minusDays(1);

        applyDateToMessage(user, messageId, date, userMessage);

        userStateStorage.setDefault(user.getId());
    }

    @Override
    public void handleWaitForDataCommand(User user, int messageId, CallbackCommand command) {
        var optionalUserMessage = userMessageStorage.findById(messageId, user.getId());
        if (optionalUserMessage.isEmpty()) {
            editInlineMessage(user, "Данные отсутствуют. Возврат в главное меню.",
                    KeyboardUtils.createMainMenu(), messageId);
            return;
        }

        var userMessage = optionalUserMessage.get();
        UserState state = userStateStorage.findByUserId(user.getId())
                .orElseGet(() -> {
                    var ns = UserState.builder()
                            .role(UserRole.USER)
                            .userId(user.getId())
                            .state(UserStates.WAIT_FOR_DATA_INPUT)
                            .botMessageId(messageId)
                            .build();
                    userStateStorage.create(ns);
                    return ns;
                });

        state.setBotMessageId(messageId);

        state.setState(UserStates.WAIT_FOR_DATA_INPUT);
        state.setBotMessageId(messageId);
        var messageState = userMessage.getState();
        long newMessageId = messageId;
        switch (messageState) {
            case WAIT_FOR_DATA -> {
                var newText = MessageUtils.createTextSum(userMessage.getSum(), false);
                newText += MessageUtils.prepareHtml("\nОжидание даты");
                newMessageId = editInlineMessage(user, newText,
                        KeyboardUtils.createUnchangableMarkup(), messageId);
            }
            case SAVED_CHANGABLE -> {
                var optionalExpense = expenseStorage.findById(
                        userMessage.getExpenseId());
                if (optionalExpense.isEmpty()) {
                    var newText = MessageUtils.createHtml("Данная трата была удалена. " +
                            "Выберите команду или введите новую трату");
                    editInlineMessage(user, newText,
                            KeyboardUtils.createMainMenu(), messageId);
                    // TO DO delete
                    userStateStorage.setDefault(user.getId());
                    return;
                }
                var expense = optionalExpense.get();
                var newText = MessageUtils.createTextTag(
                        expense.getSum(), expense.getDate(), tagStorage, expense.getTagId());
                newText += MessageUtils.prepareHtml("\nОжидание даты");
                newMessageId = editInlineMessage(user, newText,
                        KeyboardUtils.createUnchangableMarkup(), messageId);
            }
        }
        userMessage.setMessageId(newMessageId);
        userMessageStorage.update(userMessage, messageId);
        userStateStorage.update(state);
    }

    @Override
    public void handleDateMessage(User user, String messageText) {
        var optionalState = userStateStorage.findByUserId(user.getId());
        if (optionalState.isEmpty()) {
            sendTextMessage(user, "Ошибка получения");
            return;
        }

        var userState = optionalState.get();
        var optionalMessage = userMessageStorage.findById(userState.getBotMessageId(), user.getId());
        if (optionalMessage.isEmpty()) {
            sendTextMessage(user, "Ошибка: данные траты отсутствуют");
            userStateStorage.setDefault(user.getId());
            return;
        }

        if (!InputDetector.isDate(messageText)) {
            sendTextMessage(user, "Ошибка: вы ввели дату неправильно. Попробуйте ещё раз.");
            return;
        }

        var date = LocalDate.parse(messageText, formatter);
        applyDateToMessage(user, (int) userState.getBotMessageId(), date, optionalMessage.get());
        userStateStorage.setDefault(user.getId());
    }

    @Override
    public void handleSettingTag(User user, int tagId, int messageId) {
        var optionalMessage = userMessageStorage.findById(messageId, user.getId());
        if (optionalMessage.isEmpty()) {
            sendTextMessage(user, "Ошибка: данные траты отсутствуют");
            userStateStorage.setDefault(user.getId());
            return;
        }

        long newMessageId = messageId;
        var userMessage = optionalMessage.get();
        var optionalTag = tagStorage.findById(tagId);
        if (optionalTag.isEmpty()) {
            var message = MessageUtils.createTextData(userMessage.getSum(),
                    userMessage.getDate(), false);
            newMessageId = editInlineMessage(user, message,
                    KeyboardUtils.createTagKeyboard(user, tagStorage, CallbackCommand.SET_TAG), messageId);
            userMessage.setMessageId(newMessageId);
            userMessageStorage.update(userMessage, messageId);
            return;
        }
        userMessage.setTagId(tagId);
        var optionalExpense = expenseStorage.findById(userMessage.getExpenseId());
        if (optionalExpense.isEmpty() && !userMessage.getState().equals(MessageState.SAVED_CHANGABLE)) {
            Expense expense = Expense.builder()
                    .tagId(tagId)
                    .userId(user.getId())
                    .sum(userMessage.getSum())
                    .date(userMessage.getDate())
                    .build();
            var optionalNewExpense = expenseStorage.create(expense);
            if (optionalNewExpense.isEmpty()) {
                return;
            }
            int expenseId = optionalNewExpense.get().getId();
            userMessage.setExpenseId(expenseId);
        } else if (optionalExpense.isEmpty()) {
            String message = "Данная трата была удалена";
            newMessageId = editInlineMessage(user, message,
                    KeyboardUtils.createUnchangableMarkup(), messageId);
            userMessage.setState(MessageState.UNCHANGABLE);
            userMessage.setMessageId(newMessageId);
            userMessageStorage.update(userMessage, messageId);
            return;
        }

        optionalExpense = expenseStorage.findById(userMessage.getExpenseId());
        if (optionalExpense.isEmpty()) {
            String message = "Данная трата была удалена";
            newMessageId = editInlineMessage(user, message,
                    KeyboardUtils.createUnchangableMarkup(), messageId);
            userMessage.setState(MessageState.UNCHANGABLE);
            userMessage.setMessageId(newMessageId);
            userMessageStorage.update(userMessage, messageId);
            return;
        }

        var expense = optionalExpense.get();
        expense.setTagId(tagId);
        expenseStorage.update(expense);

        newMessageId = editInlineMessage(user,
                MessageUtils.createTextTag(userMessage.getSum(), userMessage.getDate(), tagStorage, tagId),
                KeyboardUtils.createSavedChangableMarkup(), messageId);

        userMessage.setMessageId(newMessageId);
        userMessage.setState(MessageState.SAVED_CHANGABLE);
        userMessageStorage.update(userMessage, messageId);
    }

    private void applyDateToMessage(User user, int messageId, LocalDate date, UserMessage userMessage) {
        long newMessageId = messageId;
        userMessage.setDate(date);

        switch (userMessage.getState()) {
            case WAIT_FOR_DATA -> {
                String message = MessageUtils.createTextData(
                        userMessage.getSum(), date, true);
                var keyboard = KeyboardUtils.createTagKeyboard(
                        user, tagStorage, CallbackCommand.SET_TAG);
                newMessageId = editInlineMessage(user, message, keyboard, messageId);
                userMessage.setState(MessageState.WAIT_FOR_TAG);
            }
            case SAVED_CHANGABLE -> {
                var optionalExpense = expenseStorage.findById(
                        userMessage.getExpenseId());
                if (optionalExpense.isEmpty()) {
                    var newText = MessageUtils.createHtml("Данная трата была удалена. " +
                            "Выберите команду или введите новую трату");
                    editInlineMessage(user, newText,
                            KeyboardUtils.createMainMenu(), messageId);
                    // TO DO delete
                    userStateStorage.setDefault(user.getId());
                    return;
                }

                var expense = optionalExpense.get();
                expense.setDate(date);
                expenseStorage.update(expense);

                var keyboard = KeyboardUtils.createSumKeyboard(
                        userMessage.getState(), user, tagStorage
                );
                var newText = MessageUtils.createTextTag(
                        expense.getSum(), date, tagStorage, expense.getTagId());
                if (newText.contains("Тег этой траты был удален")) {
                    keyboard = KeyboardUtils.createMainMenu();
                    // TO DO delete
                    userStateStorage.setDefault(user.getId());
                    editInlineMessage(user, newText, keyboard, messageId);
                    return;
                }
                newMessageId = editInlineMessage(user, newText, keyboard, messageId);
            }
        }
        userMessage.setMessageId(newMessageId);
        userMessageStorage.update(userMessage, messageId);
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

    public long sendTextMessage(User user, String text) {
        var message = SendMessage.builder()
                .chatId(user.getChatId())
                .text(text)
                .parseMode(ParseMode.HTML)
                .build();
        try {
            var mes = telegramClient.execute(message);
            return mes.getMessageId();
        } catch (TelegramApiException e) {
            log.error(String.format("Exception while sending message:\n%s\nMessage: %s", message.toString(), e.getMessage()));
            return 0;
        }
    }

    public long sendInlineMessage(User user, String text, InlineKeyboardMarkup markup) {
        var message = SendMessage.builder()
                .chatId(user.getChatId())
                .text(text)
                .parseMode(ParseMode.HTML)
                .replyMarkup(markup)
                .build();
        try {
            var mes = telegramClient.execute(message);
            return mes.getMessageId();
        } catch (TelegramApiException e) {
            log.error(String.format("Exception while sending message:\n%s\nMessage: %s", message.toString(), e.getMessage()));
            return 0;
        }
    }

    public long editInlineMessage(User user, String text, InlineKeyboardMarkup markup, int messageId) {
        var editMessage = EditMessageText.builder()
                .messageId(messageId)
                .text(text)
                .replyMarkup(markup)
                .chatId(user.getChatId())
                .build();
        try {
            telegramClient.execute(editMessage);
            return messageId;
        } catch (TelegramApiException e) {
            log.error(String.format("Exception while editing message with id:\n%s\nError message: %s", messageId, e.getMessage()));
            return sendInlineMessage(user, text, markup);
        }
    }

    public static boolean isMoreThanTwoDaysAgo(LocalDateTime dateTime) {
        return Duration.between(dateTime, LocalDateTime.now()).toDays() > 2;
    }
}
