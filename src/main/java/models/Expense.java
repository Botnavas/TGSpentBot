package models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Expense {
    int id;
    long uid;
    int tagId;
    LocalDate date;
    int sum;

    public Expense(long uid, int tagId, LocalDate date, int sum) {
        this.uid = uid;
        this.tagId = tagId;
        this.date = date;
        this.sum = sum;
    }
}
