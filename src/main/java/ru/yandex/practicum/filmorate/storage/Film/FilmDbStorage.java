package ru.yandex.practicum.filmorate.storage.Film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    @Override
    public Collection<Film> getAllFilms() {
        String sql = "SELECT * FROM FILMS";  // LEFT JOIN больше не нужен
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper);
        films.forEach(this::loadGenresAndLikes);
        return films;
    }

    @Override
    public Optional<Film> getFilmById(Long id) {
        String sql = "SELECT * FROM FILMS WHERE id = ?";
        try {
            Film film = jdbcTemplate.queryForObject(sql, filmRowMapper, id);
            if (film != null) {
                loadGenresAndLikes(film);
            }
            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Film addFilm(Film film) {
        String sql = """
                INSERT INTO FILMS (name, description, release_date, duration, rate_id)
                VALUES (?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setObject(5, film.getRate() != null ? film.getRate().getId() : null);
            return ps;
        }, keyHolder);

        Map<String, Object> keys = keyHolder.getKeys();
        if (keys == null || !keys.containsKey("ID")) {
            throw new RuntimeException("Failed to retrieve film id");
        }
        Long newId = ((Number) keys.get("ID")).longValue();
        film.setId(newId);

        // Сохраняем жанры
        if (film.getGenre() != null && !film.getGenre().isEmpty()) {
            saveGenres(newId, film.getGenre());
        }

        // Сохраняем лайки
        if (film.getLikes() != null && !film.getLikes().isEmpty()) {
            saveLikes(newId, film.getLikes());
        }

        return getFilmById(newId).orElseThrow();
    }

    @Override
    public Film updateFilm(Film film) {
        String sql = """
                UPDATE FILMS 
                SET name = ?, description = ?, release_date = ?, duration = ?, rate_id = ?, update_time = CURRENT_TIMESTAMP
                WHERE id = ?
                """;
        int updated = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                java.sql.Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getRate() != null ? film.getRate().getId() : null,
                film.getId()
        );

        if (updated == 0) {
            throw new RuntimeException("Film not found with id: " + film.getId());
        }

        // Обновляем жанры
        deleteGenresByFilmId(film.getId());
        if (film.getGenre() != null && !film.getGenre().isEmpty()) {
            saveGenres(film.getId(), film.getGenre());  // ← передаем List<Integer> genre
        }

        // Обновляем лайки
        if (film.getLikes() != null) {
            deleteLikesByFilmId(film.getId());
            if (!film.getLikes().isEmpty()) {
                saveLikes(film.getId(), film.getLikes());
            }
        }

        return getFilmById(film.getId()).orElseThrow();
    }

    @Override
    public boolean deleteFilm(Long id) {
        String sql = "DELETE FROM FILMS WHERE id = ?";
        int deleted = jdbcTemplate.update(sql, id);
        return deleted > 0;
    }

    @Override
    public long getNextId() {
        String sql = "SELECT NEXT VALUE FOR FILMS_SEQUENCE";
        Long maxId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM FILMS", Long.class);
        return (maxId == null ? 1 : maxId + 1);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        log.info("=== Добавление лайка: film={}, user={} ===", filmId, userId);


        String checkSql = "SELECT COUNT(*) FROM LINK_FILMLIKES WHERE film_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, filmId, userId);

        if (count != null && count > 0) {
            log.warn("Лайк уже существует: film={}, user={}", filmId, userId);
            return;
        }


        String sql = "INSERT INTO LINK_FILMLIKES (film_id, user_id) VALUES (?, ?)";
        try {
            int result = jdbcTemplate.update(sql, filmId, userId);
            log.info("Лайк добавлен: {} строк", result);
        } catch (Exception e) {
            log.error("Ошибка при добавлении лайка: {}", e.getMessage());
            throw new RuntimeException("Не удалось добавить лайк", e);
        }
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        log.info("=== Удаление лайка: film={}, user={} ===", filmId, userId);

        String sql = "DELETE FROM LINK_FILMLIKES WHERE film_id = ? AND user_id = ?";
        int result = jdbcTemplate.update(sql, filmId, userId);
        log.info("Результат удаления лайка: {} строк затронуто", result);
    }

    @Override
    public Set<Long> getLikes(Long filmId) {
        String sql = "SELECT user_id FROM LINK_FILMLIKES WHERE film_id = ?";
        List<Long> likes = jdbcTemplate.queryForList(sql, Long.class, filmId);
        return new HashSet<>(likes);
    }


    private void loadGenresAndLikes(Film film) {
        loadGenres(film);
        loadLikes(film);
    }

    private void loadGenres(Film film) {
        String sql = "SELECT genre_id FROM LINK_FILMGENRE WHERE film_id = ? ORDER BY id";
        List<Integer> genreIds = jdbcTemplate.queryForList(sql, Integer.class, film.getId());
        film.setGenre(genreIds);
        log.debug("Загружено {} жанров для фильма {}", genreIds.size(), film.getId());
    }

    private void loadLikes(Film film) {
        String sql = "SELECT user_id FROM LINK_FILMLIKES WHERE film_id = ?";
        List<Long> likes = jdbcTemplate.queryForList(sql, Long.class, film.getId());
        film.setLikes(new HashSet<>(likes));
        log.debug("Загружено {} лайков для фильма {}", likes.size(), film.getId());
    }

    private void saveGenres(Long filmId, List<Integer> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            log.debug("Нет жанров для сохранения для фильма {}", filmId);
            return;
        }

        String sql = "INSERT INTO LINK_FILMGENRE (film_id, genre_id) VALUES (?, ?)";
        List<Object[]> batchArgs = genreIds.stream()
                .map(genreId -> new Object[]{filmId, genreId})
                .toList();

        if (!batchArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batchArgs);
            log.info("Сохранено {} жанров для фильма {}", batchArgs.size(), filmId);
        }
    }

    private void saveLikes(Long filmId, Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return;
        String sql = "INSERT INTO LINK_FILMLIKES (film_id, user_id) VALUES (?, ?)";
        List<Object[]> batchArgs = userIds.stream()
                .map(userId -> new Object[]{filmId, userId})
                .toList();
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    private void deleteGenresByFilmId(Long filmId) {
        jdbcTemplate.update("DELETE FROM LINK_FILMGENRE WHERE film_id = ?", filmId);
    }

    private void deleteLikesByFilmId(Long filmId) {
        jdbcTemplate.update("DELETE FROM LINK_FILMLIKES WHERE film_id = ?", filmId);
    }
}