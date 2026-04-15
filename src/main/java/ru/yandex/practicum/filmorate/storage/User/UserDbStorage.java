package ru.yandex.practicum.filmorate.storage.User;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.FriendshipType;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Component
@RequiredArgsConstructor
@Primary
@Qualifier("userDbStorage")
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Override
    public Collection<User> getAllUsers() {
        String sql = "SELECT * FROM USERS";
        List<User> users = jdbcTemplate.query(sql, userRowMapper);
        return users;
    }

    @Override
    public Optional<User> getUserById(Long id) {
        String sql = "SELECT * FROM USERS WHERE id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, id);
            if (user != null) {
                loadFriends(user);
            }
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public User addUser(User user) {
        String sql = """
                INSERT INTO USERS (login, name, email, birthday)
                VALUES (?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getLogin());
            ps.setString(2, user.getName());
            ps.setString(3, user.getEmail());
            ps.setDate(4, user.getBirthday() != null ? java.sql.Date.valueOf(user.getBirthday()) : null);
            return ps;
        }, keyHolder);

        // Получаем сгенерированный ID
        Map<String, Object> keys = keyHolder.getKeys();
        if (keys == null || !keys.containsKey("ID")) {
            throw new RuntimeException("Failed to retrieve user id");
        }
        Long newId = ((Number) keys.get("ID")).longValue();
        user.setId(newId);


        return getUserById(newId).orElseThrow();
    }

    @Override
    public User updateUser(User user) {
        String sql = """
                UPDATE USERS 
                SET login = ?, name = ?, email = ?, birthday = ?, update_time = CURRENT_TIMESTAMP
                WHERE id = ?
                """;

        int updated = jdbcTemplate.update(sql,
                user.getLogin(),
                user.getName(),
                user.getEmail(),
                user.getBirthday() != null ? java.sql.Date.valueOf(user.getBirthday()) : null,
                user.getId()
        );

        if (updated == 0) {
            throw new RuntimeException("User not found with id: " + user.getId());
        }

        // Обновляем друзей: удаляем старые, вставляем новые
        deleteFriendsByUserId(user.getId());
        if (user.getFriends() != null && !user.getFriends().isEmpty()) {
            saveFriends(user.getId(), user.getFriends());
        }

        return getUserById(user.getId()).orElseThrow();
    }

    @Override
    public void addFriend(Long userId, Long friendId, boolean confirmed) {
        int typeId = confirmed ? FriendshipType.CONFIRMED.getId() : FriendshipType.UNCONFIRMED.getId();
        String sql = "INSERT INTO FRIENDSHIPS (user_id, friend_id, friendship_type_id) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, userId, friendId, typeId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        String sql = "DELETE FROM FRIENDSHIPS WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }


    @Override
    public Set<Long> getFriends(Long userId) {
        String sql = "SELECT friend_id FROM FRIENDSHIPS WHERE user_id = ? AND friendship_type_id = ?";
        List<Long> friends = jdbcTemplate.queryForList(sql, Long.class, userId, FriendshipType.CONFIRMED.getId());
        return new HashSet<>(friends);
    }


    @Override
    public Set<Long> getCommonFriends(Long userId, Long otherUserId) {
        Set<Long> userFriends = getFriends(userId);
        Set<Long> otherUserFriends = getFriends(otherUserId);
        userFriends.retainAll(otherUserFriends);
        return userFriends;
    }

    private void loadFriends(User user) {
        String sql = "SELECT friend_id FROM FRIENDSHIPS WHERE user_id = ? AND friendship_type_id = ?";
        List<Long> friends = jdbcTemplate.queryForList(sql, Long.class, user.getId(), FriendshipType.CONFIRMED.getId());
        user.setFriends(new HashSet<>(friends));
    }

    private void saveFriends(Long userId, Set<Long> friendIds) {
        if (friendIds == null || friendIds.isEmpty()) return;

        String sql = "INSERT INTO FRIENDSHIPS (user_id, friend_id, friendship_type_id) VALUES (?, ?, ?)";
        List<Object[]> batchArgs = friendIds.stream()
                .map(friendId -> new Object[]{userId, friendId, FriendshipType.CONFIRMED.getId()})
                .toList();
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    private void deleteFriendsByUserId(Long userId) {
        jdbcTemplate.update("DELETE FROM FRIENDSHIPS WHERE user_id = ?", userId);
    }


    public List<User> getFriendsWithDetails(Long userId) {
        String sql = """
        SELECT u.* FROM USERS u
        JOIN FRIENDSHIPS f ON u.id = f.friend_id
        WHERE f.user_id = ? AND f.friendship_type_id = ?
        """;
        return jdbcTemplate.query(sql, userRowMapper, userId, FriendshipType.CONFIRMED.getId());
    }

    @Override
    public List<User> getCommonFriendsWithDetails(Long userId, Long otherId) {
        String sql = """
        SELECT u.* FROM USERS u
        WHERE u.id IN (
            SELECT f1.friend_id FROM FRIENDSHIPS f1
            WHERE f1.user_id = ? AND f1.friendship_type_id = ?
            INTERSECT
            SELECT f2.friend_id FROM FRIENDSHIPS f2
            WHERE f2.user_id = ? AND f2.friendship_type_id = ?
        )
        """;

        return jdbcTemplate.query(sql, userRowMapper,
                userId, FriendshipType.CONFIRMED.getId(),
                otherId, FriendshipType.CONFIRMED.getId()
        );
    }
}