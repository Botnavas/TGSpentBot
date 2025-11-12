package utilites;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class Utilites {

    public static void main(String[] args) {
        List<List<String>> calendar = generateMonth(2025, 10);
        for (var row : calendar) {
            System.out.println(row);
        }
    }

    public static List<List<String>> generateMonth(int year, int month) {
        List<List<String>> rows = new ArrayList<>();
        YearMonth ym = YearMonth.of(year, month);
        int daysInMonth = ym.lengthOfMonth();

        LocalDate firstDay = ym.atDay(1);
        int firstDayOfWeek = firstDay.getDayOfWeek().getValue(); // 1 = Пн, 7 = Вс

        List<String> week = new ArrayList<>();

        // добавляем пустые клетки до первого дня месяца
        for (int i = 1; i < firstDayOfWeek; i++) {
            week.add(" ");
        }

        // добавляем дни
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate current = LocalDate.of(year, month, day);
            DayOfWeek dow = current.getDayOfWeek();

            // Можно красиво подписывать день недели, если хочешь:
            String label = day + "," + shortDayName(dow);

            week.add(label);

            // если неделя закончилась — добавляем в список и начинаем новую
            if (dow == DayOfWeek.SUNDAY) {
                rows.add(new ArrayList<>(week));
                week.clear();
            }
        }

        // если последняя неделя не заполнена
        if (!week.isEmpty()) {
            while (week.size() < 7) {
                week.add(" ");
            }
            rows.add(week);
        }

        return rows;
    }

    // вспомогательная функция для коротких имён
    private static String shortDayName(DayOfWeek dow) {
        return switch (dow) {
            case MONDAY -> "пн";
            case TUESDAY -> "вт";
            case WEDNESDAY -> "ср";
            case THURSDAY -> "чт";
            case FRIDAY -> "пт";
            case SATURDAY -> "сб";
            case SUNDAY -> "вс";
        };
    }
}
