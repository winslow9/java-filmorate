package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTests {

    private UserController userController;
    private User validUser;

    @BeforeEach
    void setUp() {
        userController = new UserController();
        validUser = User.builder()
                .email("test@test.com")
                .login("validlogin")
                .name("Test Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
    }

    @Test
    void createUser_WithValidData_ShouldSuccess() {
        User created = userController.create(validUser);

        assertNotNull(created.getId());
        assertEquals("test@test.com", created.getEmail());
        assertEquals("validlogin", created.getLogin());
        assertEquals("Test Name", created.getName());
        assertEquals(LocalDate.of(1990, 1, 1), created.getBirthday());
    }


    @Test
    void createUser_WithBlankEmail_ShouldThrowValidationException() {
        User user = User.builder()
                .email("")
                .login("validlogin")
                .name("Test Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void createUser_WithEmailWithoutAtSymbol_ShouldThrowValidationException() {
        User user = User.builder()
                .email("test.test.com")
                .login("validlogin")
                .name("Test Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        assertThrows(ValidationException.class, () -> userController.create(user));
    }


    @Test
    void createUser_WithBlankLogin_ShouldThrowValidationException() {
        User user = User.builder()
                .email("test@test.com")
                .login("")
                .name("Test Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void createUser_WithLoginContainingSpaces_ShouldThrowValidationException() {
        User user = User.builder()
                .email("test@test.com")
                .login("valid login")
                .name("Test Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void createUser_WithEmptyName_ShouldUseLoginAsName() {
        User user = User.builder()
                .email("test@test.com")
                .login("validlogin")
                .name(null)
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User created = userController.create(user);

        assertEquals("validlogin", created.getName());
    }

    @Test
    void createUser_WithBirthdayInFuture_ShouldThrowValidationException() {
        User user = User.builder()
                .email("test@test.com")
                .login("validlogin")
                .name("Test Name")
                .birthday(LocalDate.now().plusDays(1))
                .build();

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void createUser_WithBirthdayToday_ShouldSuccess() {
        User user = User.builder()
                .email("test@test.com")
                .login("validlogin")
                .name("Test Name")
                .birthday(LocalDate.now())
                .build();

        User created = userController.create(user);

        assertEquals(LocalDate.now(), created.getBirthday());
    }

    @Test
    void updateUser_WithValidData_ShouldSuccess() {
        User created = userController.create(validUser);

        User updatedUser = User.builder()
                .id(created.getId())
                .email("updated@test.com")
                .login("updatedlogin")
                .name("Updated Name")
                .birthday(LocalDate.of(1995, 1, 1))
                .build();

        User result = userController.update(updatedUser);

        assertEquals("updated@test.com", result.getEmail());
        assertEquals("updatedlogin", result.getLogin());
        assertEquals("Updated Name", result.getName());
        assertEquals(LocalDate.of(1995, 1, 1), result.getBirthday());
    }

    @Test
    void updateUser_WithNullId_ShouldThrowValidationException() {
        User user = User.builder()
                .email("test@test.com")
                .login("validlogin")
                .name("Test Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        assertThrows(ValidationException.class, () -> userController.update(user));
    }

}