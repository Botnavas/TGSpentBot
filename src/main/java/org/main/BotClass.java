package org.main;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;

public class BotClass implements LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient;
    private DBInterface db;
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

            SendMessage message = createReply(chatId, messageText);

            try {
                telegramClient.execute(message); // Sending our message object to user
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private SendMessage createReply(long id, String text)
    {
        switch (text) {
            case "/start" -> {
                db.setLastCommand(id, text);
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
                user.setLastCommand(text);
                db.changeUser(user);
                SendMessage message = showTagList(id);
                String newText = message.getText() + "Введите новый тег";
                message.setText(newText);
                return message;
            }

            case "Удалить тег" -> {
                User user = db.getUser(id);
                user.setLastCommand(text);
                db.changeUser(user);
                SendMessage message = deleteTags(id);
                return message;
            }

            case "Вернуться назад" -> {
                User user = db.getUser(id);
                String lastCommand = user.getLastCommand();
                switch (lastCommand) {
                    case "Удалить тег" -> {
                        db.setLastCommand(id, " ");
                        return createStartMenu(id);
                    }
                    default -> {
                        db.setLastCommand(id, "");
                        return createStartMenu(id);
                    }
                }
            }
            default -> {
                User user = db.getUser(id);
                String lastCommand = user.getLastCommand();
                switch (lastCommand) {
                    case "Добавить тег" -> {
                        if (db.addTag(id,text)) {
                            db.setLastCommand(id, "Тег добавлен");
                            SendMessage message = addedTag(id);
                            return message;
                        }
                        else {
                            db.setLastCommand(id, "");
                            SendMessage message = addedTag(id);
                            message.setText("Тег не добавлен. Выберите действие:");
                            return message;
                        }
                    }
                    case "Удалить тег" -> {
                        if (db.deleteTag(id, text)) {
                            db.setLastCommand(id, "Тег удален");
                            return deletedTag(id, text, true);
                        }
                        else {
                            db.setLastCommand(id, "Тег не удален");
                            return deletedTag(id, text, false);
                        }
                    }
                    default -> {
                        db.setLastCommand(id, "");
                        return createStartMenu(id);
                    }
                }
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
                //keyboardMarkup.setOneTimeKeyboard(false); // Не скрывать после нажатия

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
                row1.add("Занести трату");
                row1.add("Добавить тег");
                KeyboardRow row2 = new KeyboardRow();
                row2.add("Удалить тег");

                // Первая строка

                keyboard.add(row1);
                keyboard.add(row2);

                ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup(keyboard);
                keyboardMarkup.setResizeKeyboard(true); // Автоматически подгонять размер
                //keyboardMarkup.setOneTimeKeyboard(false); // Не скрывать после нажатия

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

    private SendMessage showTagList (long id) {
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

    private SendMessage addedTag(long id)
    {
        SendMessage message = createStartMenu(id);
        message.setText("Тег добавлен\\. Выберите действие\\:");
        return message;
    }

    private SendMessage deleteTags(long id) {
        List<String> tags = db.getTags(id);
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

        row = new KeyboardRow();
        row.add("Вернуться назад");
        keyboardRows.add(row);

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup(keyboardRows);
        SendMessage message = SendMessage.builder()
                .chatId(id)
                .text("Выберите тег для удаления\\.\n" +
                        MessageFormatter.bold("Все связанные траты будут удалены\\!"))
                .parseMode("MarkdownV2")
                .build();
        message.setReplyMarkup(markup);
        return message;
    }

    SendMessage deletedTag(long id, String text, boolean success) {
        SendMessage message = createStartMenu(id);
        if (success) {
            message.setText("Тег " + MessageFormatter.bold(text) + " успешно удален\\. Выберите команду\\.");
        }
        else {
            message.setText("Тег " + MessageFormatter.bold(text) + "не удален\\. Выберите команду\\.");
        }
        return message;
    }
}
