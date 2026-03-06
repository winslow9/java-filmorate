package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.Film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.User.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final LocalDate FIRST_FILM_DATE = LocalDate.of(1895, 12, 28);

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film getFilmById(Long id) {
        return filmStorage.getFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
    }

    public Film createFilm(Film film) {
        validateFilm(film);

        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }

        // Установка ID
        long nextId = getNextId();
        film.setId(nextId);

        filmStorage.addFilm(film);
        log.info("Создан фильм с id: {}", film.getId());
        return film;
    }

    public Film updateFilm(Film film) {
        if (film.getId() == null) {
            throw new ValidationException("ID фильма должен быть указан");
        }

        Film existingFilm = getFilmById(film.getId());

        if (film.getName() != null && !film.getName().isBlank()) {
            existingFilm.setName(film.getName());
        }

        if (film.getDescription() != null) {
            validateDescription(film.getDescription());
            existingFilm.setDescription(film.getDescription());
        }

        if (film.getReleaseDate() != null) {
            validateReleaseDate(film.getReleaseDate());
            existingFilm.setReleaseDate(film.getReleaseDate());
        }

        if (film.getDuration() != null) {
            validateDuration(film.getDuration());
            existingFilm.setDuration(film.getDuration());
        }

        filmStorage.updateFilm(existingFilm);
        log.info("Обновлен фильм с id: {}", film.getId());
        return existingFilm;
    }

    public void addLike(Long filmId, Long userId) {
        Film film = getFilmById(filmId);
        User user = getUserById(userId);

        film.getLikes().add(userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        Film film = getFilmById(filmId);
        User user = getUserById(userId);

        film.getLikes().remove(userId);
        log.info("Пользователь {} удалил лайк с фильма {}", userId, filmId);
    }

    public List<Film> getTopFilms(Integer count) {
        if (count == null || count <= 0) {
            count = 10; // значение по умолчанию
        }

        return filmStorage.getAllFilms().stream()
                .sorted(Comparator.comparingInt(
                        (Film f) -> f.getLikes().size()
                ).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    private void validateFilm(Film film) {
        validateName(film.getName());
        validateDescription(film.getDescription());
        validateReleaseDate(film.getReleaseDate());
        validateDuration(film.getDuration());
    }

    private void validateName(String name) {
        if (name == null || name.trim().isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }
    }

    private void validateDescription(String description) {
        if (description != null && description.length() > 200) {
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
    }

    private void validateReleaseDate(LocalDate releaseDate) {
        if (releaseDate != null && releaseDate.isBefore(FIRST_FILM_DATE)) {
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
    }

    private void validateDuration(Integer duration) {
        if (duration != null && duration <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }

    private long getNextId() {
        return filmStorage.getAllFilms().stream()
                .mapToLong(Film::getId)
                .max()
                .orElse(0) + 1;
    }

    private User getUserById(Long userId) {
        return userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    public List<Film> getPopularFilms(Integer count) {
        // Если count не указан или меньше 1, используем значение по умолчанию
        int limit = (count == null || count < 1) ? 10 : count;

        log.info("Получение топ-{} фильмов по количеству лайков", limit);

        return filmStorage.getAllFilms().stream()
                .sorted(Comparator.comparingInt(
                        (Film film) -> film.getLikes().size()
                ).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}