package ru.yandex.practicum.filmorate.tests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.Film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.Film.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.Genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.Genre.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.MPA.MpaDBStorage;
import ru.yandex.practicum.filmorate.storage.MPA.MpaRowMapper;
import ru.yandex.practicum.filmorate.storage.User.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.User.UserRowMapper;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({
        UserDbStorage.class,
        FilmDbStorage.class,
        UserRowMapper.class,
        FilmRowMapper.class,
        GenreDbStorage.class,
        GenreRowMapper.class,
        MpaDBStorage.class,
        MpaRowMapper.class
})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmoRateApplicationTests {

    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;
    private final GenreDbStorage genreStorage;
    private final MpaDBStorage mpaStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Очищаем таблицы перед каждым тестом
        jdbcTemplate.execute("DELETE FROM LINK_FILMLIKES");
        jdbcTemplate.execute("DELETE FROM LINK_FILMGENRE");
        jdbcTemplate.execute("DELETE FROM FRIENDSHIPS");
        jdbcTemplate.execute("DELETE FROM FILMS");
        jdbcTemplate.execute("DELETE FROM USERS");

        // Сбрасываем ID
        jdbcTemplate.execute("ALTER TABLE USERS ALTER COLUMN ID RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE FILMS ALTER COLUMN ID RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE LINK_FILMLIKES ALTER COLUMN ID RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE LINK_FILMGENRE ALTER COLUMN ID RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE FRIENDSHIPS ALTER COLUMN ID RESTART WITH 1");

        // Заполняем словари заново
        jdbcTemplate.execute("MERGE INTO DICT_MPA (id, name) VALUES (1, 'G'), (2, 'PG'), (3, 'PG-13'), (4, 'R'), (5, 'NC-17')");
        jdbcTemplate.execute("MERGE INTO DICT_FILMGENRES (id, name) VALUES (1, 'Комедия'), (2, 'Драма'), (3, 'Мультфильм'), (4, 'Триллер'), (5, 'Документальный'), (6, 'Боевик')");
        jdbcTemplate.execute("MERGE INTO DICT_FRIENDSHIPTYPE (id, name) VALUES (1, 'Подтвержденная'), (2, 'Неподтвержденная')");
    }

    // ==================== ТЕСТЫ ПОЛЬЗОВАТЕЛЕЙ ====================

    @Test
    void testFindUserById_ShouldReturnUser_WhenUserExists() {
        User user = User.builder()
                .login("testuser")
                .name("Test User")
                .email("test@example.com")
                .birthday(LocalDate.of(1990, 1, 1))
                .friends(new HashSet<>())
                .build();
        User createdUser = userStorage.addUser(user);

        Optional<User> userOptional = userStorage.getUserById(createdUser.getId());
        assertThat(userOptional).isPresent();
        assertThat(userOptional.get().getLogin()).isEqualTo("testuser");
    }

    @Test
    void testFindUserById_ShouldReturnEmpty_WhenUserNotExists() {
        Optional<User> userOptional = userStorage.getUserById(999L);
        assertThat(userOptional).isEmpty();
    }

    @Test
    void testAddUser_ShouldGenerateId() {
        User newUser = User.builder()
                .login("newuser")
                .name("New User")
                .email("newuser@example.com")
                .birthday(LocalDate.of(2000, 1, 1))
                .friends(new HashSet<>())
                .build();
        User createdUser = userStorage.addUser(newUser);
        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getId()).isGreaterThan(0);
        assertThat(createdUser.getLogin()).isEqualTo("newuser");
    }

    @Test
    void testUpdateUser_ShouldUpdateExistingUser() {
        User user = User.builder()
                .login("updateuser")
                .name("Original Name")
                .email("update@example.com")
                .birthday(LocalDate.of(1990, 1, 1))
                .friends(new HashSet<>())
                .build();
        User createdUser = userStorage.addUser(user);

        createdUser.setName("Updated Name");
        User updatedUser = userStorage.updateUser(createdUser);
        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
    }

    @Test
    void testAddFriend_ShouldAddFriendship() {
        User user1 = User.builder()
                .login("user1")
                .name("User 1")
                .email("user1@example.com")
                .birthday(LocalDate.of(1990, 1, 1))
                .friends(new HashSet<>())
                .build();
        User user2 = User.builder()
                .login("user2")
                .name("User 2")
                .email("user2@example.com")
                .birthday(LocalDate.of(1991, 2, 2))
                .friends(new HashSet<>())
                .build();
        user1 = userStorage.addUser(user1);
        user2 = userStorage.addUser(user2);

        userStorage.addFriend(user1.getId(), user2.getId(), true);
        Set<Long> friends = userStorage.getFriends(user1.getId());
        assertThat(friends).contains(user2.getId());
    }

    @Test
    void testRemoveFriend_ShouldRemoveFriendship() {
        User user1 = User.builder()
                .login("user1")
                .name("User 1")
                .email("user1@example.com")
                .birthday(LocalDate.of(1990, 1, 1))
                .friends(new HashSet<>())
                .build();
        User user2 = User.builder()
                .login("user2")
                .name("User 2")
                .email("user2@example.com")
                .birthday(LocalDate.of(1991, 2, 2))
                .friends(new HashSet<>())
                .build();
        user1 = userStorage.addUser(user1);
        user2 = userStorage.addUser(user2);

        userStorage.addFriend(user1.getId(), user2.getId(), true);
        userStorage.removeFriend(user1.getId(), user2.getId());
        Set<Long> friends = userStorage.getFriends(user1.getId());
        assertThat(friends).doesNotContain(user2.getId());
    }

    // ==================== ТЕСТЫ ФИЛЬМОВ ====================

    @Test
    void testFindFilmById_ShouldReturnFilm_WhenFilmExists() {
        Film film = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2024, 1, 1))
                .duration(120)
                .rate(ru.yandex.practicum.filmorate.model.MPA.PG13)
                .genre(new ArrayList<>())
                .likes(new HashSet<>())
                .build();
        Film createdFilm = filmStorage.addFilm(film);

        Optional<Film> filmOptional = filmStorage.getFilmById(createdFilm.getId());
        assertThat(filmOptional).isPresent();
        assertThat(filmOptional.get().getName()).isEqualTo("Test Film");
    }

    @Test
    void testFindFilmById_ShouldReturnEmpty_WhenFilmNotExists() {
        Optional<Film> filmOptional = filmStorage.getFilmById(999L);
        assertThat(filmOptional).isEmpty();
    }

    @Test
    void testAddFilm_ShouldGenerateId() {
        Film newFilm = Film.builder()
                .name("New Film")
                .description("Description")
                .releaseDate(LocalDate.of(2024, 1, 1))
                .duration(100)
                .rate(ru.yandex.practicum.filmorate.model.MPA.PG)
                .genre(new ArrayList<>())
                .likes(new HashSet<>())
                .build();
        Film createdFilm = filmStorage.addFilm(newFilm);
        assertThat(createdFilm.getId()).isNotNull();
        assertThat(createdFilm.getId()).isGreaterThan(0);
        assertThat(createdFilm.getName()).isEqualTo("New Film");
    }

    @Test
    void testUpdateFilm_ShouldUpdateExistingFilm() {
        Film film = Film.builder()
                .name("Original Film")
                .description("Original Description")
                .releaseDate(LocalDate.of(2024, 1, 1))
                .duration(120)
                .rate(ru.yandex.practicum.filmorate.model.MPA.PG13)
                .genre(new ArrayList<>())
                .likes(new HashSet<>())
                .build();
        Film createdFilm = filmStorage.addFilm(film);

        createdFilm.setName("Updated Film Name");
        Film updatedFilm = filmStorage.updateFilm(createdFilm);
        assertThat(updatedFilm.getName()).isEqualTo("Updated Film Name");
    }

    @Test
    void testAddLike_ShouldAddLikeToFilm() {
        User user = User.builder()
                .login("liker")
                .name("Liker")
                .email("liker@example.com")
                .birthday(LocalDate.of(1990, 1, 1))
                .friends(new HashSet<>())
                .build();
        user = userStorage.addUser(user);

        Film film = Film.builder()
                .name("Film")
                .description("Description")
                .releaseDate(LocalDate.of(2024, 1, 1))
                .duration(120)
                .rate(ru.yandex.practicum.filmorate.model.MPA.PG13)
                .genre(new ArrayList<>())
                .likes(new HashSet<>())
                .build();
        film = filmStorage.addFilm(film);

        filmStorage.addLike(film.getId(), user.getId());
        Set<Long> likes = filmStorage.getLikes(film.getId());
        assertThat(likes).contains(user.getId());
    }

    @Test
    void testRemoveLike_ShouldRemoveLikeFromFilm() {
        User user = User.builder()
                .login("liker")
                .name("Liker")
                .email("liker@example.com")
                .birthday(LocalDate.of(1990, 1, 1))
                .friends(new HashSet<>())
                .build();
        user = userStorage.addUser(user);

        Film film = Film.builder()
                .name("Film")
                .description("Description")
                .releaseDate(LocalDate.of(2024, 1, 1))
                .duration(120)
                .rate(ru.yandex.practicum.filmorate.model.MPA.PG13)
                .genre(new ArrayList<>())
                .likes(new HashSet<>())
                .build();
        film = filmStorage.addFilm(film);

        filmStorage.addLike(film.getId(), user.getId());
        filmStorage.removeLike(film.getId(), user.getId());
        Set<Long> likes = filmStorage.getLikes(film.getId());
        assertThat(likes).doesNotContain(user.getId());
    }

    // ==================== ТЕСТЫ ЖАНРОВ ====================

    @Test
    void testGetAllGenres_ShouldReturnAllGenres() {
        var genres = genreStorage.getAllGenres();
        assertThat(genres).isNotEmpty();
        assertThat(genres).hasSize(6);
    }

    // ==================== ТЕСТЫ MPA ====================

    @Test
    void testGetAllMpa_ShouldReturnAllMpa() {
        var mpaList = mpaStorage.findAll();
        assertThat(mpaList).hasSize(5);
    }

    @Test
    void testGetMpaById_ShouldReturnMpa_WhenMpaExists() {
        var mpaOptional = mpaStorage.findById(1);
        assertThat(mpaOptional).isPresent();
        assertThat(mpaOptional.get().getName()).isEqualTo("G");
    }
}