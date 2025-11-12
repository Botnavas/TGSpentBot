package dev.botnavas.tgspentbot;

import models.Expense;
import models.Tag;
import models.User;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import utilites.AppConfig;
import utilites.InputDetector;
import utilites.MessageFormatter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

enum CallbackCommand {
    YESTERDAY("ytd"),
    TODAY("tdy"),
    SELECT_DATE("dat"),
    SELECT_TAG("tag"),
    EDIT("edt"),
    CHANGE_SUM("sum"),
    CHANGE_DATE("cdt"),
    CHANGE_TAG("ctg"),
    DELETE("del"),
    ADD_TAG("addTag"),
    DATE_SELECTED("dateSelected"),
    DELETE_TAG("delTag"),
    MESSAGE_TO_DEL("mtd"),
    UNKNOWN("");

    private final String shortCommand;
    CallbackCommand(String shortCommand) {
        this.shortCommand = shortCommand;
    }

    public String getShortCommand() {
        return shortCommand;
    }
    public static CallbackCommand fromShortCommand(String shortCommand) {
        for (CallbackCommand cmd : CallbackCommand.values()) {
            if (cmd.shortCommand.equals(shortCommand)) {
                return cmd;
            }
        }
        return UNKNOWN;
    }
}
public class BotClass implements LongPollingSingleThreadUpdateConsumer {
    private static final Logger logger = LoggerFactory.getLogger(BotClass.class);
    private final TelegramClient telegramClient;
    private final DBInterface db;
    private final DateTimeFormatter dtf = new DateTimeFormatterBuilder()
            .appendPattern("dd.MM.yyyy")
            .parseDefaulting(ChronoField.ERA, 1) // 1 = н.э. (AD)
            .toFormatter()
            .withResolverStyle(ResolverStyle.STRICT);

    public BotClass(String botToken) {
        telegramClient = new OkHttpTelegramClient(botToken);
        db = new DBClass(AppConfig.getDatabaseUrl());
    }
    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            // Set variables
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String name = update.getMessage().getFrom().getFirstName();
            db.createUser(name, chatId);

            /*SendMessage message = SendMessage // Create a message object
                    .builder()
                    .chatId(chatId)
                    .text(messageText)
                    .build();*/

            String lastCommands = db.getUser(chatId).getLastCommand();
            if(lastCommands.length() > 1) {
                handleCommandedMessage(chatId, messageText, lastCommands);
                logger.info("Получено сообщение {}, последняя команада {}", messageText, lastCommands);
                return;
            }
            logger.info("Получено сообщение {}, последняя команада отсутствует", messageText);
            SendMessage message = createReply(chatId, messageText);

            try {
                telegramClient.execute(message); // Sending our message object to user
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }   else if (update.hasCallbackQuery()) {
            handleCallback(update);
        }
    }

    private void handleCommandedMessage(long id, String text, String lastCommands) {
        if (text.equals("Вернуться назад")) {
            handleCancelMessage(id, lastCommands);
            return;
        }
        SendMessage message;
        boolean needSending = false;
        //User user = db.getUser(id);
        String[] commands = lastCommands.split(";");
        CallbackCommand command = CallbackCommand.fromShortCommand(commands[0]);
        switch (command) {
            case ADD_TAG -> {
                if (db.addTag(id,text)) {
                    db.clearLastCommand(id);
                    message = createAddedTagReply(id);
                    needSending = true;
                }
                else {
                    db.clearLastCommand(id);
                    message = createAddedTagReply(id);
                    message.setText("Тег не добавлен. Выберите действие:");
                    needSending = true;
                }
            }
            case DELETE_TAG -> {
                if (db.deleteTag(id, text)) {
                    db.clearLastCommand(id);
                    message =  createDeletedTagReply(id, text, true);
                    needSending = true;
                }
                else {
                    db.clearLastCommand(id);
                    message =  createDeletedTagReply(id, text, false);
                    needSending = true;
                }
            }
            case SELECT_DATE -> {
                dateInputedHandler(commands, text, id);
                return;
            }
            case MESSAGE_TO_DEL -> {
                int mid = Integer.parseInt(commands[1]);
                DeleteMessage dm = DeleteMessage.builder()
                        .chatId(id)
                        .messageId(mid).build();
                db.clearLastCommand(id);
                try {
                    telegramClient.execute(dm); // Sending our message object to user
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                return;
            }
            default -> {
                message =  createStartMenu(id);
                db.clearLastCommand(id);
                needSending = true;
            }
        }

        if (needSending) {
            try {
                telegramClient.execute(message); // Sending our message object to user
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    //Command; sum; mid; midToDel
    private void dateInputedHandler(String[] data, String text, long uid) {
        if (InputDetector.getInputType(text).equals(InputDetector.InputType.Date)) {
            data[0] = CallbackCommand.DATE_SELECTED.getShortCommand();
            int mid = Integer.parseInt(data[2]);
            data[2] = text;
            db.clearLastCommand(uid);
            selectDateHandler(data, mid, uid);
            if(data.length == 3) {
                return;
            }
            try {
                DeleteMessage message = DeleteMessage.builder()
                        .messageId(Integer.parseInt(data[3]))
                        .chatId(uid)
                        .build();
                telegramClient.execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            return;
        }
        SendMessage message = SendMessage.builder()
                .chatId(uid)
                .text("Вы ввели дату не в том формате. " +
                        "Нажмите кнопку выбора даты заново.")
                .build();
        db.clearLastCommand(uid);
        try {
          int mid = telegramClient.execute(message).getMessageId();
          db.setLastCommand(uid, CallbackCommand.MESSAGE_TO_DEL.getShortCommand()
                  + ";" + mid);//We delete this message next time
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleCancelMessage(long id, String lastCommands) {
        User user = db.getUser(id);
        String[] commands = lastCommands.split(";");
        String lastCommand = commands[0];
        SendMessage message;
        boolean needSendMessage = false;
        switch (lastCommand) {
            default -> {
                db.clearLastCommand(id);
                needSendMessage = true;
                message = createStartMenu(id);
            }
        }

        if (needSendMessage) {
            try {
                telegramClient.execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleCallback(Update update) {
        CallbackQuery query = update.getCallbackQuery();
        boolean isOld = !(query.getMessage() instanceof Message);
        long uid = isOld? query.getMessage().getChat().getId(): query.getMessage().getChatId();
        String callbackData = query.getData();
        String[] data = callbackData.split(";");
        int mid = query.getMessage().getMessageId();

        EditMessageText emt = new EditMessageText("");
        EditMessageReplyMarkup emrm = new EditMessageReplyMarkup();
        SendMessage sm = new SendMessage("", "");

        CallbackCommand command = CallbackCommand.fromShortCommand(data[0]);
        switch (command) {
            case TODAY, YESTERDAY, SELECT_DATE -> {
                    selectDateHandler(data, mid, uid);
            }
            case SELECT_TAG -> {
                selectTagHandler(data, mid, uid);
            }
            default -> {

            }
        }

        AnswerCallbackQuery ack = new AnswerCallbackQuery(query.getId());
        ack.setCallbackQueryId(query.getId());
        try {
            telegramClient.execute(ack);
            //ADD MES DEL
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void selectDateHandler(String[] data, int mid, long uid) {
        CallbackCommand command = CallbackCommand.fromShortCommand(data[0]);
        LocalDate date;
        int sum = Integer.parseInt(data[1]);
        switch (command) {
            case TODAY -> {
                date = LocalDate.now();
            }
            case YESTERDAY -> {
                date = LocalDate.now().minusDays(1);
            }
            case DATE_SELECTED -> {
                date = LocalDate.parse(data[2], dtf);
            }
            default -> {
                date = null;
            }
        }

        if (date != null) {
            String text = generateDateFirstInputText(sum, date);
            EditMessageText emt = EditMessageText.builder()
                    .chatId(uid)
                    .messageId(mid)
                    .parseMode("MarkdownV2")
                    .replyMarkup(buildTagSelectionInlineKeyboard(uid, sum, date.format(dtf)))
                    .text(text).build();
            try {
                telegramClient.execute(emt); // Sending our message object to user
            } catch (TelegramApiException e) {
                e.printStackTrace();
                SendMessage message = SendMessage.builder()
                        .chatId(uid)
                        .parseMode("MarkdownV2")
                        .replyMarkup(buildTagSelectionInlineKeyboard(uid, sum, date.format(dtf)))
                        .text(text).build();
                try {
                    telegramClient.execute(message);
                }  catch (TelegramApiException ee) {
                    ee.printStackTrace();
                }
            }
        } else {
            String lastCommand = CallbackCommand.SELECT_DATE.getShortCommand()
                    + ";"
                    + sum
                    + ";"
                    + mid;
            db.setLastCommand(uid, lastCommand);
            SendMessage message= SendMessage.builder()
                    .chatId(uid)
                    .text("Введите дату в формате\n" + MessageFormatter.bold("ДД.ММ.ГГГ"))
                    .parseMode("MarkdownV2")
                    .build();
            try {
               int id = telegramClient.execute(message).getMessageId();
               db.setLastCommand(uid, lastCommand + ";" + id);
            }  catch (TelegramApiException ee) {
                ee.printStackTrace();
            }
        }
    }

    //sum;date;tagId
    private void selectTagHandler(String[] data, int mid, long uid) {
        int sum = Integer.parseInt(data[1]);
        LocalDate date = LocalDate.parse(data[2], dtf);
        int tagId = Integer.parseInt(data[3]);

        if (db.checkTag(uid, tagId)) {
            Expense expense = new Expense(uid, tagId, date, sum);
            int eid = db.addExpense(expense);
            String tag = db.getTagById(tagId).getTag();
            String messageText = generateTagFirstInputText(sum, date, tag);
            EditMessageText emt = EditMessageText.builder()
                    .messageId(mid)
                    .chatId(uid)
                    .text(messageText)
                    .parseMode("MarkdownV2")
                    .replyMarkup(buildExpenseChangeInlineKeyboard(eid))
                    .build();
            try {
                telegramClient.execute(emt);
            } catch (TelegramApiException e) {
                e.printStackTrace();
                SendMessage message = SendMessage.builder()
                        .chatId(uid)
                        .text(messageText)
                        .parseMode("MarkdownV2")
                        .replyMarkup(buildExpenseChangeInlineKeyboard(eid))
                        .build();
                try {
                    telegramClient.execute(message);
                } catch (TelegramApiException ee) {
                    ee.printStackTrace();
                }
            }
        }
    }

    private SendMessage createReply(long id, String text)
    {
        switch (text) {
            case "/start" -> {
                db.clearLastCommand(id);
                return createStartMenu(id);
            }
            case "Зарегистрироваться" -> {
                User user = db.getUser(id);
                user.setStatus(User.UserStatus.REGISTERED);
                user.setLastCommand(text);
                db.changeUser(user);
                SendMessage message = createStartMenu(id);
                if (message != null) {
                    message.setText("Вы зарегистрированы Выберите команду.");
                }
                return message;

            }
            case "Добавить тег" -> {
                User user = db.getUser(id);
                user.setLastCommand(CallbackCommand.ADD_TAG.getShortCommand());
                db.changeUser(user);
                SendMessage message = createTagListMessage(id);
                String newText = message.getText() + "Введите новый тег";
                message.setText(newText);
                return message;
            }

            case "Удалить тег" -> {
                User user = db.getUser(id);
                user.setLastCommand(CallbackCommand.DELETE_TAG.getShortCommand());
                db.changeUser(user);
                SendMessage message = createDeleteTagMenu(id);
                return message;
            }
            default -> {
                db.clearLastCommand(id);
                if (InputDetector.getInputType(text).equals(InputDetector.InputType.Number)) {
                    return answerToNumber(id, (int)Double.parseDouble(text));
                }
                return createStartMenu(id);
            }
        }
    }

    private SendMessage createStartMenu(long id)
    {
        if (!db.checkUser(id)) {
            return null;
        }

        User user = db.getUser(id);
        switch (user.getStatus()) {
            case UNREGISTERED -> {

                List<KeyboardRow> keyboard = new ArrayList<>();
                KeyboardRow row1 = new KeyboardRow();
                row1.add("Зарегистрироваться");
                // Первая строка

                keyboard.add(row1);

                ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup(keyboard);
                keyboardMarkup.setResizeKeyboard(true); // Автоматически подгонять размер
                keyboardMarkup.setOneTimeKeyboard(false); // Не скрывать после нажатия

                SendMessage message = SendMessage.builder()
                        .chatId(id)
                        .text("Выберите действие:")
                        .build();

                message.setReplyMarkup(keyboardMarkup);
                return message;
            }
            case REGISTERED -> {

                List<KeyboardRow> keyboard = new ArrayList<>();
                KeyboardRow row1 = new KeyboardRow();
                //row1.add("Занести трату");
                row1.add("Добавить тег");
                KeyboardRow row2 = new KeyboardRow();
                row2.add("Удалить тег");

                // Первая строка

                keyboard.add(row1);
                keyboard.add(row2);

                ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup(keyboard);
                keyboardMarkup.setResizeKeyboard(true); // Автоматически подгонять размер
                keyboardMarkup.setOneTimeKeyboard(false); // Не скрывать после нажатия

                SendMessage message = SendMessage.builder()
                        .chatId(id)
                        .text("Выберите действие\\:")
                        .parseMode("MarkdownV2")
                        .build();

                message.setReplyMarkup(keyboardMarkup);
                return message;
            }
        }

        return null;
    }

    private SendMessage createTagListMessage(long id) {
        List<String> tags = db.getTags(id);
        StringBuilder tagsBuilder = new StringBuilder("Текущие теги:\n");
        for (String tag : tags) {
            tagsBuilder.append(MessageFormatter.bold(tag)).append("\n");
        }
        SendMessage message = SendMessage.builder()
                .chatId(id)
                .text(tagsBuilder.toString())
                .parseMode("MarkdownV2")
                .build();
        return message;
    }

    private SendMessage createAddedTagReply(long id)
    {
        SendMessage message = createStartMenu(id);
        message.setText("Тег добавлен\\. Выберите действие\\:");
        return message;
    }

    private SendMessage createDeleteTagMenu(long id) {
        List<String> tags = db.getTags(id);
        List<KeyboardRow> keyboardRows = buildTagKeyboardRows(tags);

        KeyboardRow row = new KeyboardRow();
        row.add("Вернуться назад");
        keyboardRows.add(row);

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup(keyboardRows);
        SendMessage message = SendMessage.builder()
                .chatId(id)
                .text("Выберите тег для удаления\\.\n" +
                        MessageFormatter.bold("Все связанные траты будут удалены!"))
                .parseMode("MarkdownV2")
                .build();
        message.setReplyMarkup(markup);
        return message;
    }

    @NotNull
    private static List<KeyboardRow> buildTagKeyboardRows(List<String> tags) {
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        int counter = 0;
        KeyboardRow row = new KeyboardRow();
        for (var tag : tags) {
            if ((counter % 2) == 0 && counter != 0) {
                keyboardRows.add(row);
            }
            if ((counter % 2) == 0) {
                 row = new KeyboardRow();
            }

            row.add(tag);
            counter++;

            if (counter == tags.size()) {
                keyboardRows.add(row);
            }
        }

        return keyboardRows;
    }

    SendMessage createDeletedTagReply(long id, String text, boolean success) {
        SendMessage message = createStartMenu(id);
        if (success) {
            message.setText("Тег " + MessageFormatter.bold(text) + " успешно удален\\. Выберите команду\\.");
        }
        else {
            message.setText("Тег " + MessageFormatter.bold(text) + "не удален\\. Выберите команду\\.");
        }
        return message;
    }

    private SendMessage answerToNumber(long id, int sum) {
        InlineKeyboardMarkup markup = buildDateSelectionInlineKeyboard(id, sum);

        String text = generateSumFirstInputText(sum);

        SendMessage message = SendMessage.builder()
                .chatId(id)
                .text(text)
                .parseMode("MarkdownV2")
                .replyMarkup(markup)
                .build();
        return message;
    }

    private InlineKeyboardMarkup buildDateSelectionInlineKeyboard(long id, int sum)
    {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        List<InlineKeyboardRow> rows = new ArrayList<>();
        InlineKeyboardButton yesterday = InlineKeyboardButton.builder()
                .text("Вчера")
                .callbackData(CallbackCommand.YESTERDAY.getShortCommand() + ";" + sum)
                .build();
        buttons.add(yesterday);
        InlineKeyboardButton today = InlineKeyboardButton.builder()
                .text("Сегодня")
                .callbackData(CallbackCommand.TODAY.getShortCommand() + ";" + sum)
                .build();
        buttons.add(today);
        InlineKeyboardButton chooseDate = InlineKeyboardButton.builder()
                .text("Выбрать дату")
                .callbackData(CallbackCommand.SELECT_DATE.getShortCommand() + ";" + sum)
                .build();
        buttons.add(chooseDate);
        InlineKeyboardRow row = new InlineKeyboardRow(buttons);
        rows.add(row);
        return new InlineKeyboardMarkup(rows);
    }

    private InlineKeyboardMarkup buildTagSelectionInlineKeyboard(long uid, int sum, String date)
    {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        List<Tag> tags = db.getTagsModels(uid);
        for (Tag tag : tags)
        {
            String callbackData = new StringBuilder()
                    .append(CallbackCommand.SELECT_TAG.getShortCommand())
                    .append(";")
                    .append(sum)
                    .append(";")
                    .append(date)
                    .append(";")
                    .append(tag.getId()).toString();
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .callbackData(callbackData)
                    .text(tag.getTag()).build();
            InlineKeyboardRow row = new InlineKeyboardRow(button);
            rows.add(row);
        }

        return new InlineKeyboardMarkup(rows);
    }

    private InlineKeyboardMarkup buildExpenseChangeInlineKeyboard(int expenseId)
    {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        InlineKeyboardButton button = InlineKeyboardButton.builder()
                .text("Изменить дату")
                .callbackData(CallbackCommand.CHANGE_DATE.getShortCommand() + ";" + expenseId)
                .build();
        InlineKeyboardRow row = new InlineKeyboardRow(button);
        rows.add(row);
        button = InlineKeyboardButton.builder()
                .text("Изменить тег")
                .callbackData(CallbackCommand.CHANGE_TAG.getShortCommand() + ";" + expenseId)
                .build();
        row = new InlineKeyboardRow(button);
        rows.add(row);
        button = InlineKeyboardButton.builder()
                .text("Изменить сумму")
                .callbackData(CallbackCommand.CHANGE_SUM.getShortCommand() + ";" + expenseId)
                .build();
        row = new InlineKeyboardRow(button);
        rows.add(row);
        button = InlineKeyboardButton.builder()
                .text("Удалить")
                .callbackData(CallbackCommand.DELETE.getShortCommand() + ";" + expenseId)
                .build();
        row = new InlineKeyboardRow(button);
        rows.add(row);
        return new InlineKeyboardMarkup(rows);
    }

    private String generateSumFirstInputText(int sum) {
        return new StringBuilder()
                .append("\uD83D\uDCB8")
                .append(MessageFormatter.bold("Вы ввели: "))
                .append(sum)
                .append("\n")
                .append("\uD83D\uDCC5 Выберите дату")
                .toString();
    }

    private String generateDateFirstInputText(int sum, LocalDate date) {
        String message =  generateSumFirstInputText(sum).
                replace("Выберите дату",
                        date.format(dtf).replace(".", "\\."));
       return message + "\n" + "\uD83C\uDFF7\uFE0F" + MessageFormatter.bold("Выберите тег");
    }

    private String generateTagFirstInputText(int sum, LocalDate date, String tag) {
        String message = generateDateFirstInputText(sum, date)
                .replace(MessageFormatter.bold("Выберите тег"), tag);
        return message + "\n" + MessageFormatter.bold("Трата записана");
    }
}
