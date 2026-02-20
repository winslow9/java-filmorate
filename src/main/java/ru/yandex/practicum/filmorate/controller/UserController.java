package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();
    LocalDate today = LocalDate.now();

    @GetMapping
    public Collection<User> getAll() {
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Email не может быть пустым и должен содержать @");
        }
        if (user.getLogin().trim() == null || user.getLogin().trim().isBlank() || user.getLogin().trim().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getBirthday().isAfter(today)) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

        if (users.isEmpty()) {
            user.setId(1L);
        } else {
            user.setId(getNextId());
        }
        user.setEmail(user.getEmail());
        if (user.getName() == null) {
            user.setName(user.getLogin());
        } else {
            user.setName(user.getName());
        }
        user.setBirthday(user.getBirthday());
        user.setLogin(user.getLogin());
        users.put(user.getId(), user);

        log.info("Создана сущность с id " + user.getId());
        return users.get(user.getId());
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        if (user.getId() == null) {
            throw new ValidationException("ID пользователя должен быть указан");
        }
        if (users.keySet().contains(user.getId())) {
            User oldUser = users.get(user.getId());
            if (user.getEmail() != null) {
                oldUser.setEmail(user.getEmail());
            }
            if (user.getName() != null) {
                oldUser.setName(user.getName());
            }
            if (user.getLogin() != null) {
                oldUser.setLogin(user.getLogin());
            }
            if (user.getBirthday() != null) {
                oldUser.setBirthday(user.getBirthday());
            }

            log.info("Изменена сущность с id " + user.getId());
            return oldUser;
        } else {
            throw new NotFoundException("Пользак с таким id не найден");
        }
    }

}




