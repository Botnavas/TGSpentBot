package dev.botnavas.tgspentbot.utils;

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
}
