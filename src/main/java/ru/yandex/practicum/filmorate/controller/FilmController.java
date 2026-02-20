package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();
    LocalDate firstDate = LocalDate.of(1895, 12, 28);

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @GetMapping
    public Collection<Film> getAll(){
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film){
        if (film.getName() == null || film.getName().trim().isBlank()){
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription().length() > 200){
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate().isBefore(firstDate)){
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        if (film.getDuration()<=0){
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }

        if (films.isEmpty()){
            film.setId(1L);
        } else {
            film.setId(getNextId());
        }

        film.setDescription(film.getDescription());
        film.setName(film.getName());
        film.setDuration(film.getDuration());
        film.setReleaseDate(film.getReleaseDate());
        films.put(film.getId(), film);

        log.info("Создан фильм с id "+film.getId());
        return films.get(film.getId());
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film){
        if (film.getId() == null){
            throw new ValidationException("ID пользователя должен быть указан");
        } if (films.keySet().contains(film.getId())){
            Film oldFilm = films.get(film.getId());
            if (film.getDuration() != null){
                oldFilm.setDescription(film.getDescription());
            }
            if (film.getName() != null){
                oldFilm.setName(film.getName());
            }
            if (film.getDuration() != null){
                oldFilm.setDuration(film.getDuration());
            }
            if (film.getReleaseDate() != null){
                oldFilm.setReleaseDate(film.getReleaseDate());
            }
            log.info("Изменен фильм с id "+film.getId());
            return oldFilm;
        }else
        { throw new NotFoundException("Фильм с таким id не найден");}
    }

}
