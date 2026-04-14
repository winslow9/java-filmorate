package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FilmGenreLink {
    private Long id;
    private Long filmId;
    private Integer genreId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}