package org.main;

public class MessageFormatter {

    public static String bold(String text) {
        return "*" + escapeMarkdown(text) + "*";
    }

    public static String italic(String text) {
        return "_" + escapeMarkdown(text) + "_";
    }

    public static String code(String text) {
        return "`" + escapeMarkdown(text) + "`";
    }

    public static String codeBlock(String text, String language) {
        return "```" + language + "\n" + text + "\n```";
    }

    public static String quote(String text) {
        return "> " + text.replace("\n", "\n> ");
    }

    public static String link(String text, String url) {
        return "[" + escapeMarkdown(text) + "](" + url + ")";
    }

    private static String escapeMarkdown(String text) {
        if (text == null) return "";

        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
