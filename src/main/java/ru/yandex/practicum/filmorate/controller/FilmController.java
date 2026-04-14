package ru.yandex.practicum.filmorate.controller;


import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@RestController
@Validated
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<FilmDto> getAll() {
        log.info("Запрос на получение всех фильмов");
        return filmService.getAllFilms().stream()
                .map(filmService::convertToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public FilmDto getFilmById(@Positive @PathVariable Long id) {
        log.info("Запрос на получение фильма с id: {}", id);
        Film film = filmService.getFilmById(id);
        return filmService.convertToDto(film);
    }

    @PostMapping
    public FilmDto create(@Valid @RequestBody Film film) {
        log.info("=== ПОЛУЧЕННЫЙ FILM ===");
        log.info("name: {}", film.getName());
        log.info("description: {}", film.getDescription());
        log.info("releaseDate: {}", film.getReleaseDate());
        log.info("duration: {}", film.getDuration());
        log.info("rate: {}", film.getRate());
        log.info("genres: {}", film.getGenre());
        log.info("======================");

        Film createdFilm = filmService.createFilm(film);
        FilmDto filmDto = filmService.convertToDto(createdFilm);
        log.info("=== ВОЗВРАЩАЕМЫЙ FilmDto ===");
        log.info("genres in DTO: {}", filmDto.getGenres());
        log.info("==========================");
        return filmDto;
    }

    @PutMapping
    public FilmDto update(@Valid @RequestBody Film film) {
        log.info("Запрос на обновление фильма с id: {}", film.getId());
        Film updatedFilm = filmService.updateFilm(film);
        return filmService.convertToDto(updatedFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@Positive @PathVariable Long id, @Positive @PathVariable Long userId) {
        log.info("Запрос на добавление лайка: пользователь {} ставит лайк фильму {}", userId, id);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@Positive @PathVariable Long id, @Positive @PathVariable Long userId) {
        log.info("Запрос на удаление лайка: пользователь {} удаляет лайк с фильма {}", userId, id);
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<FilmDto> getPopularFilms(@RequestParam(value = "count", defaultValue = "10") Integer count) {
        log.info("Запрос на получение {} популярных фильмов", count);
        return filmService.getPopularFilms(count).stream()
                .map(filmService::convertToDto)
                .collect(Collectors.toList());
    }

}