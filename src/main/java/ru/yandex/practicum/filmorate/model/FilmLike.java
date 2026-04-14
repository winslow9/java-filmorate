package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FilmLike {
    private Long id;
    private Long filmId;
    private Long userId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}