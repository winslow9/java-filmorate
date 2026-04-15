package ru.yandex.practicum.filmorate.storage.Film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FilmStorage {
    Collection<Film> getAllFilms();

    Optional<Film> getFilmById(Long id);

    Film addFilm(Film film);

    Film updateFilm(Film film);


    void addLike(Long filmId, Long userId);


    void removeLike(Long filmId, Long userId);


    Set<Long> getLikes(Long filmId);

    List<Film> getPopularFilmsWithLikes(int limit);
}