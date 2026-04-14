package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
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

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }

        User createdUser = userStorage.addUser(user);
        log.info("Создан пользователь с id: {}", createdUser.getId());
        return createdUser;
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
        log.info("=== Добавление друга: user={}, friend={} ===", userId, friendId);

        // Проверяем, что оба пользователя существуют
        getUserById(userId);
        getUserById(friendId);

        // Добавляем друга через storage
        userStorage.addFriend(userId, friendId, true);

        // Для взаимной дружбы (опционально)
        // userStorage.addFriend(friendId, userId, true);

        log.info("Друг успешно добавлен: {} -> {}", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        log.info("=== Удаление друга: user={}, friend={} ===", userId, friendId);

        getUserById(userId);
        getUserById(friendId);


        userStorage.removeFriend(userId, friendId);

        log.info("Друг успешно удален: {} -> {}", userId, friendId);
    }

    public Collection<User> getFriends(Long userId) {
        log.info("=== Получение друзей пользователя: {} ===", userId);

        getUserById(userId);

        Set<Long> friendIds = userStorage.getFriends(userId);
        log.info("Найдено ID друзей: {}", friendIds);

        List<User> friends = friendIds.stream()
                .map(this::getUserById)
                .collect(Collectors.toList());

        log.info("Возвращено друзей: {}", friends.size());
        return friends;
    }

    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        log.info("=== Получение общих друзей пользователей {} и {} ===", userId, otherId);

        getUserById(userId);
        getUserById(otherId);

        Set<Long> commonFriendIds = userStorage.getCommonFriends(userId, otherId);
        log.info("Найдено общих ID друзей: {}", commonFriendIds);

        List<User> commonFriends = commonFriendIds.stream()
                .map(this::getUserById)
                .collect(Collectors.toList());

        log.info("Возвращено общих друзей: {}", commonFriends.size());
        return commonFriends;
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
}