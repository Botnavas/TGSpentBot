package dev.botnavas.tgspentbot.utilites;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;

public class InputDetector {

    public static void main(String[] args) {
        System.out.println(getInputType("11.10.2025"));
    }
    public enum InputType {
        Date,
        Number,
        Invalid
    }

    public static boolean isDate(String input) {
        if (!input.matches("^\\d{2}\\.\\d{2}\\.\\d{4}$")) {
            return false;
        }

        try {
            DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                    .appendPattern("dd.MM.yyyy")
                    .parseDefaulting(ChronoField.ERA, 1) // 1 = н.э. (AD)
                    .toFormatter()
                    .withResolverStyle(ResolverStyle.STRICT);
            LocalDate.parse(input, formatter);
        } catch (DateTimeParseException e) {
            return false;
        }

        return true;
    }

    public static boolean isNumber(String input) {
        String normalizedInput = input.replace(',', '.');

        try {
            Double.parseDouble(normalizedInput);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    public static InputType getInputType(String input) {
        if (isDate(input)) {
            return InputType.Date;
        } else if (isNumber(input)) {
            return InputType.Number;
        } else {
            return InputType.Invalid;
        }
    }
}
