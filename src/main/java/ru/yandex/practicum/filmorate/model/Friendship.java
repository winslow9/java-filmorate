package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Friendship {
    private Long id;
    private Long userId;
    private Long friendId;
    private FriendshipType type; // тип дружбы (подтвержденная/неподтвержденная)
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}