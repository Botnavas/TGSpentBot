package org.main;

import java.time.LocalDate;

public class Expense {
    long id;
    String tag;
    LocalDate date;

    public Expense(long id, String tag, LocalDate date) {
        this.id = id;
        this.tag = tag;
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
