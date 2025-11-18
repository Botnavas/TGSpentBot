package dev.botnavas.tgspentbot.utils;

import dev.botnavas.tgspentbot.tag.model.Tag;
import dev.botnavas.tgspentbot.tag.storage.TagStorage;
import dev.botnavas.tgspentbot.user.service.model.CallbackCommand;
import dev.botnavas.tgspentbot.user.model.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class KeyboardUtils {
    public static InlineKeyboardMarkup createMainMenu() {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        InlineKeyboardButton button = InlineKeyboardButton.builder()
                .text("Добавить тег")
                .callbackData(CallbackCommand.NEW_TAG.toString())
                .build();
        InlineKeyboardRow row = new InlineKeyboardRow(button);
        rows.add(row);

        button = InlineKeyboardButton.builder()
                .text("Удалить тег")
                .callbackData(CallbackCommand.TAG_DELETE_MENU.toString())
                .build();
        row = new InlineKeyboardRow(button);
        rows.add(row);

        button = InlineKeyboardButton.builder()
                .text("Посмотреть статистику")
                .callbackData(CallbackCommand.STAT_MENU.toString())
                .build();
        row = new InlineKeyboardRow(button);
        rows.add(row);

        return new InlineKeyboardMarkup(rows);
    }

    public static InlineKeyboardMarkup createTagKeyboard(User user, TagStorage storage, CallbackCommand command) {
        List<Tag> tags = storage.findUserTagsByUserId(user.getId());
        InlineKeyboardRow row;
        List<InlineKeyboardRow> rows = new ArrayList<>();
        InlineKeyboardButton button;

        for (Tag tag : tags) {
            button = InlineKeyboardButton.builder()
                    .text(tag.getName())
                    .callbackData(command.toString() + ";" + tag.getId())
                    .build();
            row = new InlineKeyboardRow(button);
            rows.add(row);
        }

        if (command.equals(CallbackCommand.DELETE_TAG))
        {
            button = InlineKeyboardButton.builder()
                    .text("Вернуться назад")
                    .callbackData(CallbackCommand.BACK.toString())
                    .build();
            row = new InlineKeyboardRow(button);
            rows.add(row);
        }

       return new InlineKeyboardMarkup(rows);
    }
}
