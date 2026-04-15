package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.Film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.Genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MPA.MpaDBStorage;  // Добавьте импорт
import ru.yandex.practicum.filmorate.storage.User.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final MpaDBStorage mpaDBStorage;  // Добавьте поле
    private final LocalDate firstFilmDate = LocalDate.of(1895, 12, 28);

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       UserStorage userStorage,
                       GenreStorage genreStorage,
                       MpaDBStorage mpaDBStorage) {  // Добавьте в конструктор
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.mpaDBStorage = mpaDBStorage;
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

        // ПРОВЕРКА СУЩЕСТВОВАНИЯ MPA
        if (film.getRate() != null && film.getRate().getId() != 0) {
            int mpaId = film.getRate().getId();
            log.info("Проверка MPA с id: {}", mpaId);

            // Проверяем через MpaDBStorage
            boolean mpaExists = mpaDBStorage.findById(mpaId).isPresent();
            if (!mpaExists) {
                log.error("MPA с id {} не найден", mpaId);
                throw new NotFoundException("MPA с id " + mpaId + " не найден");
            }

            // Устанавливаем полный объект MPA
            MPA existingMpa = mpaDBStorage.findById(mpaId).get();
            film.setRate(existingMpa);
            log.info("MPA найден: {}", existingMpa.getName());
        }

        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }

        // Удаляем дубликаты жанров и конвертируем в List
        if (film.getGenre() != null) {
            // Убираем дубликаты, сохраняя порядок с помощью LinkedHashSet
            Set<Integer> uniqueGenres = new LinkedHashSet<>(film.getGenre());
            // Конвертируем в List
            film.setGenre(new ArrayList<>(uniqueGenres));
            log.info("Жанры после удаления дубликатов: {}", film.getGenre());
        }

        filmStorage.addFilm(film);
        log.info("Создан фильм с id: {}", film.getId());
        return film;
    }

    public Film updateFilm(Film film) {
        if (film.getId() == null) {
            throw new ValidationException("ID фильма должен быть указан");
        }

        Film existingFilm = getFilmById(film.getId());

        // Проверяем существование MPA при обновлении
        if (film.getRate() != null && film.getRate().getId() != 0) {
            MPA existingMpa = mpaDBStorage.findById(film.getRate().getId())
                    .orElseThrow(() -> new NotFoundException("MPA с id " + film.getRate().getId() + " не найден"));
            existingFilm.setRate(existingMpa);
        }

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

        // Обновляем жанры (если они переданы)
        if (film.getGenre() != null) {
            // Убираем дубликаты, сохраняя порядок
            Set<Integer> uniqueGenres = new LinkedHashSet<>(film.getGenre());
            existingFilm.setGenre(new ArrayList<>(uniqueGenres));
        }

        filmStorage.updateFilm(existingFilm);
        log.info("Обновлен фильм с id: {}", film.getId());
        return existingFilm;
    }

    public void addLike(Long filmId, Long userId) {
        log.info("Добавление лайка: фильм {}, пользователь {}", filmId, userId);

        getFilmById(filmId);
        getUserById(userId);

        filmStorage.addLike(filmId, userId);

        log.info("Лайк успешно добавлен: фильм {}, пользователь {}", filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        log.info("Удаление лайка: фильм {}, пользователь {}", filmId, userId);

        getFilmById(filmId);
        getUserById(userId);

        filmStorage.removeLike(filmId, userId);

        log.info("Лайк успешно удален: фильм {}, пользователь {}", filmId, userId);
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
        if (releaseDate != null && releaseDate.isBefore(firstFilmDate)) {
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
    }

    private void validateDuration(Integer duration) {
        if (duration != null && duration <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }


    private User getUserById(Long userId) {
        return userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    public FilmDto convertToDto(Film film) {
        if (film == null) {
            return null;
        }

        // Конвертируем MPA
        FilmDto.MpaDto mpaDto = null;
        if (film.getRate() != null) {
            mpaDto = FilmDto.MpaDto.builder()
                    .id(film.getRate().getId())
                    .name(film.getRate().getName())
                    .build();
        }

        // Конвертируем жанры из List<Integer> в List<GenreDto>
        List<FilmDto.GenreDto> genreDtos = null;
        if (film.getGenre() != null && !film.getGenre().isEmpty()) {
            genreDtos = film.getGenre().stream()
                    .map(genreId -> {
                        Genre genre = Genre.fromId(genreId);
                        if (genre != null) {
                            return FilmDto.GenreDto.builder()
                                    .id(genre.getId())
                                    .name(genre.getName())
                                    .build();
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());  // ← toList() сохраняет порядок
        }

        return FilmDto.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .mpa(mpaDto)
                .genres(genreDtos != null ? new LinkedHashSet<>(genreDtos) : null)
                .build();
    }


    public List<Film> getPopularFilms(Integer count) {
        int limit = (count == null || count < 1) ? 10 : count;
        log.info("Получение топ-{} фильмов по количеству лайков", limit);
        return filmStorage.getPopularFilmsWithLikes(limit);
    }
}