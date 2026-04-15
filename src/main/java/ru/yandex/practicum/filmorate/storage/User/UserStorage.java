package ru.yandex.practicum.filmorate.storage.User;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserStorage {
    Collection<User> getAllUsers();

    Optional<User> getUserById(Long id);

    User addUser(User user);

    User updateUser(User user);

    void addFriend(Long userId, Long friendId, boolean confirmed);

    void removeFriend(Long userId, Long friendId);

    Set<Long> getFriends(Long userId);

    List<User> getFriendsWithDetails(Long userId);

    Set<Long> getCommonFriends(Long userId, Long otherUserId);

    List<User> getCommonFriendsWithDetails(Long userId, Long otherId);
}