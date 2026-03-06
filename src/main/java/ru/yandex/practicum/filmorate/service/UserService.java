package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.User.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;
    private final LocalDate today = LocalDate.now();

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User getUserById(Long id) {
        return userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }

    public User createUser(User user) {
        validateUser(user);

        // Сетаем ID
        long nextId = getNextId();
        user.setId(nextId);

        // Сетаем имя, если не указано
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }

        userStorage.addUser(user);
        log.info("Создан пользователь с id: {}", user.getId());
        return user;
    }

    public User updateUser(User user) {
        if (user.getId() == null) {
            throw new ValidationException("ID пользователя должен быть указан");
        }

        User existingUser = getUserById(user.getId());

        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            validateEmail(user.getEmail());
            existingUser.setEmail(user.getEmail());
        }

        if (user.getLogin() != null && !user.getLogin().isBlank()) {
            validateLogin(user.getLogin());
            existingUser.setLogin(user.getLogin());
        }

        if (user.getName() != null && !user.getName().isBlank()) {
            existingUser.setName(user.getName());
        }

        if (user.getBirthday() != null) {
            validateBirthday(user.getBirthday());
            existingUser.setBirthday(user.getBirthday());
        }

        // Обновление пользователя в хранилище
        userStorage.updateUser(existingUser);
        log.info("Обновлен пользователь с id: {}", user.getId());
        return existingUser;
    }

    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Пользователь не может добавить самого себя в друзья");
        }

        User user = getUserById(userId);
        User friend = getUserById(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        log.info("Пользователь {} добавил в друзья {}", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        log.info("Пользователь {} удалил из друзей {}", userId, friendId);
    }

    public Collection<User> getFriends(Long userId) {
        User user = getUserById(userId);
        return user.getFriends().stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        User user = getUserById(userId);
        User other = getUserById(otherId);

        return user.getFriends().stream()
                .filter(friendId -> other.getFriends().contains(friendId))
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    private void validateUser(User user) {
        validateEmail(user.getEmail());
        validateLogin(user.getLogin());
        validateBirthday(user.getBirthday());
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new ValidationException("Email не может быть пустым и должен содержать @");
        }
    }

    private void validateLogin(String login) {
        if (login == null || login.isBlank() || login.contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
    }

    private void validateBirthday(LocalDate birthday) {
        if (birthday != null && birthday.isAfter(today)) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    private long getNextId() {
        return userStorage.getAllUsers().stream()
                .mapToLong(User::getId)
                .max()
                .orElse(0) + 1;
    }
}