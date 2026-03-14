package ru.yandex.practicum.filmorate.storage.User;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

@Component
public interface UserStorage {
    Collection<User> getAllUsers();

    Optional<User> getUserById(Long id);

    User addUser(User user);

    User updateUser(User user);

    boolean deleteUser(Long id);

    long getNextId();
}
