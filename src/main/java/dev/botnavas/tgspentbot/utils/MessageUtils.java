package dev.botnavas.tgspentbot.utils;

import dev.botnavas.tgspentbot.tag.storage.TagStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;

public class MessageUtils {
    public static String prepareHtml(String msg) {
        var result = msg.replace("&", "&amp;");

        result = result.replace("<", "&lt;");
        result = result.replace(">", "&gt;");
        result = result.replace("\"", "&quot;");

        return result;
    }

    public static String createHtml(String message, Object... args) {
        var objects = new Object[args.length];
        for (int i = 0; i < objects.length; i++) {
            objects[i] = prepareHtml(args[i].toString());
        }
        return String.format(message, objects);
    }

    public static String createTextSum(int sum, boolean isFirstInput) {
        String mes = "";
        if (isFirstInput) {
            mes = createHtml("Вы ввели");
        }
        mes += createHtml("\uD83D\uDCB8 <b>%s</b>", sum);
        if (isFirstInput) {
            mes += createHtml("\nВыберите дату");
        }
        return mes;
    }

    public static String createTextData(int sum, LocalDate date, boolean isFirstInput) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("dd.MM.yyyy")
                .parseDefaulting(ChronoField.ERA, 1) // 1 = н.э. (AD)
                .toFormatter()
                .withResolverStyle(ResolverStyle.STRICT);

        String mes = createTextSum(sum, false);
        mes = createHtml(mes + "\n\uD83D\uDCC5 %s", date.format(formatter));

        if (isFirstInput) {
                mes += createHtml("Выберите тег");
        }

        return mes;
    }

    public static String createTextTag(int sum, LocalDate date, TagStorage storage, int tagId) {
        String mes = createTextData(sum, date, false);

        var optionalTag = storage.findById(tagId);
        if (optionalTag.isEmpty()) {
            return createHtml("Тег этой траты был удален. Также удалены все связааные с этим тегом траты");
        }
        var tag = optionalTag.get().getName();

        return createHtml(mes + "\n\uD83C\uDFF7\uFE0F <b>%s</b>", tag);
    }
}
