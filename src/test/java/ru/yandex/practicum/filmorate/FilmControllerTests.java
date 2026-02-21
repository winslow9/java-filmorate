package ru.yandex.practicum.filmorate;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTests {

    private FilmController filmController;
    private Film validFilm;
    private static final LocalDate FIRST_FILM_DATE = LocalDate.of(1895, 12, 28);

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
        validFilm = Film.builder()
                .name("Valid Film Name")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();
    }

    @DisplayName("Добавить фильм")
    @Test
    void createFilm_WithValidData_ShouldSuccess() {
        Film created = filmController.create(validFilm);

        assertNotNull(created.getId());
        assertEquals("Valid Film Name", created.getName());
        assertEquals("Valid description", created.getDescription());
        assertEquals(LocalDate.of(2000, 1, 1), created.getReleaseDate());
        assertEquals(120, created.getDuration());
    }


    @DisplayName("Добавить фильм без названия")
    @Test
    void createFilm_WithEmptyName_ShouldThrowValidationException() {
        Film film = Film.builder()
                .name("")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void createFilm_WithNameContainingOnlySpaces_ShouldThrowValidationException() {
        Film film = Film.builder()
                .name("   ")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void createFilm_WithDescriptionExactly200Chars_ShouldSuccess() {
        String description200Chars = "a".repeat(200);
        Film film = Film.builder()
                .name("Valid Name")
                .description(description200Chars)
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Film created = filmController.create(film);

        assertEquals(200, created.getDescription().length());
    }

    @Test
    void createFilm_WithDescriptionMoreThan200Chars_ShouldThrowValidationException() {
        String description201Chars = "a".repeat(201);
        Film film = Film.builder()
                .name("Valid Name")
                .description(description201Chars)
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void createFilm_WithReleaseDateBeforeFirstFilmDate_ShouldThrowValidationException() {
        Film film = Film.builder()
                .name("Valid Name")
                .description("Valid description")
                .releaseDate(FIRST_FILM_DATE.minusDays(1))
                .duration(120)
                .build();

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void createFilm_WithReleaseDateExactlyFirstFilmDate_ShouldSuccess() {
        Film film = Film.builder()
                .name("Valid Name")
                .description("Valid description")
                .releaseDate(FIRST_FILM_DATE)
                .duration(120)
                .build();

        Film created = filmController.create(film);

        assertEquals(FIRST_FILM_DATE, created.getReleaseDate());
    }

    @Test
    void createFilm_WithReleaseDateAfterFirstFilmDate_ShouldSuccess() {
        Film film = Film.builder()
                .name("Valid Name")
                .description("Valid description")
                .releaseDate(FIRST_FILM_DATE.plusDays(1))
                .duration(120)
                .build();

        Film created = filmController.create(film);

        assertEquals(FIRST_FILM_DATE.plusDays(1), created.getReleaseDate());
    }

    @Test
    void createFilm_WithZeroDuration_ShouldThrowValidationException() {
        Film film = Film.builder()
                .name("Valid Name")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(0)
                .build();

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void createFilm_WithNegativeDuration_ShouldThrowValidationException() {
        Film film = Film.builder()
                .name("Valid Name")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(-10)
                .build();

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void createFilm_WithPositiveDuration_ShouldSuccess() {
        Film film = Film.builder()
                .name("Valid Name")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(1)
                .build();

        Film created = filmController.create(film);

        assertEquals(1, created.getDuration());
    }

    @Test
    void updateFilm_WithValidData_ShouldSuccess() {
        Film created = filmController.create(validFilm);

        Film updatedFilm = Film.builder()
                .id(created.getId())
                .name("Updated Name")
                .description("Updated description")
                .releaseDate(LocalDate.of(2010, 1, 1))
                .duration(150)
                .build();

        Film result = filmController.update(updatedFilm);

        assertEquals("Updated Name", result.getName());
        assertEquals("Updated description", result.getDescription());
        assertEquals(LocalDate.of(2010, 1, 1), result.getReleaseDate());
        assertEquals(150, result.getDuration());
    }

    @Test
    void updateFilm_WithNullId_ShouldThrowValidationException() {
        Film film = Film.builder()
                .name("Valid Name")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        assertThrows(ValidationException.class, () -> filmController.update(film));
    }


}
