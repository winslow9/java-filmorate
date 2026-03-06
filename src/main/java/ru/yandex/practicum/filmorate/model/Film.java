package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
public class Film {
    private Long id; //Comment
    @NonNull
    private String name;
    @NotBlank
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private Set<Long> likes;
}
