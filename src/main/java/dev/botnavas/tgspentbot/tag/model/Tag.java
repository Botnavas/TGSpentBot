package dev.botnavas.tgspentbot.tag.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level= AccessLevel.PRIVATE)
public class Tag {
    int id;
    long userId;
    String name;
}
