package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDate;

@Data
@Builder
public class User {
    private Long id;
    @NotBlank
    private String login;
    private String name;
    @NonNull
    @Email
    private String email;
    private LocalDate birthday;
}

