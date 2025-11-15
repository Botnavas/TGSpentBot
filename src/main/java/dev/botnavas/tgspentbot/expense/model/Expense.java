package dev.botnavas.tgspentbot.expense.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@FieldDefaults(level= AccessLevel.PRIVATE)
public class Expense {
    int id;
    long userId;
    int tagId;
    int sum;
    LocalDate date;
}
