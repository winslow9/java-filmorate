package ru.yandex.practicum.filmorate.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
public class FilmDto {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private MpaDto mpa;
    private Set<GenreDto> genres;

    @Data
    @Builder
    public static class MpaDto {
        private int id;
        private String name;
    }

    @Data
    @Builder
    public static class GenreDto {
        private int id;
        private String name;
    }
}