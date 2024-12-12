package org.example.repository;

import lombok.SneakyThrows;
import org.example.domain.User;
import org.example.domain.UserType;
import org.example.exception.LoginFailedException;
import org.example.exception.SignupFailedException;
import org.example.exception.UserLockedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;

import static org.example.TestFixtures.PASSWORD;
import static org.example.TestFixtures.SIGNUP_USERNAME1;
import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {

    private UserRepository userRepository;

    @BeforeEach
    @SneakyThrows
    public void setup() {
        URI filePath = getClass().getClassLoader().getResource("users.csv").toURI();
        userRepository = UserRepository.getInstance(new File(filePath));
    }

    @AfterEach
    public void deleteUsers() {

    }

    @Test
    public void testLoadingFile() {
        assertEquals(0, userRepository.getSize());
    }

    @Test
    public void testSignup() {
        User newUser = userRepository.signUp(SIGNUP_USERNAME1, PASSWORD);

        assertNotNull(newUser);
        assertNotNull(newUser.getId());
        assertEquals(SIGNUP_USERNAME1, newUser.getUsername());
        assertNotEquals(PASSWORD, newUser.getPassword());
        assertEquals(UserType.CUSTOMER, newUser.getType());

        User loggedInUser = userRepository.login(SIGNUP_USERNAME1, PASSWORD);
        assertEquals(newUser, loggedInUser);
    }

    @Test
    public void testDoubleSignup() {
        User newUser = userRepository.signUp(SIGNUP_USERNAME1, PASSWORD);
        assertNotNull(newUser);

        assertThrows(SignupFailedException.class, () -> userRepository.signUp(SIGNUP_USERNAME1, PASSWORD));
    }

    @Test
    public void testSignupWithUppercaseUsername() {
        assertThrows(SignupFailedException.class, () -> userRepository.signUp(SIGNUP_USERNAME1.toUpperCase(), PASSWORD));
    }

    @Test
    public void testLoginWithWrongPassword() {

        User newUser = userRepository.signUp(SIGNUP_USERNAME1, PASSWORD);

        String wrongPassword = PASSWORD.toLowerCase();
        assertThrows(LoginFailedException.class, () -> userRepository.login(SIGNUP_USERNAME1, wrongPassword));
    }

    @Test
    public void testLoginLimit() {
        User newUser = userRepository.signUp(SIGNUP_USERNAME1, PASSWORD);

        String wrongPassword = PASSWORD.toLowerCase();
        for (int i = 0; i < 3; i++) {
            assertThrows(LoginFailedException.class, () -> userRepository.login(SIGNUP_USERNAME1, wrongPassword));
        }

        assertThrows(UserLockedException.class, () -> userRepository.login(SIGNUP_USERNAME1, wrongPassword));
    }

    @Test
    public void testResetPassword() {
        userRepository.signUp(SIGNUP_USERNAME1, PASSWORD);
        User logginUser = userRepository.login(SIGNUP_USERNAME1, PASSWORD);
        String newPassword = PASSWORD + "5";
        userRepository.resetPassword(SIGNUP_USERNAME1, PASSWORD, newPassword);

        assertTrue(logginUser.matchPassword(newPassword));
    }

    @Test
    public void testLoginDeactivatedUser() {
        userRepository.signUp(SIGNUP_USERNAME1, PASSWORD);
        User logginUser = userRepository.login(SIGNUP_USERNAME1, PASSWORD);

        userRepository.deactivateUser(logginUser);
        assertThrows(LoginFailedException.class, () -> userRepository.login(SIGNUP_USERNAME1, PASSWORD));

    }

//    @Test
//    public void testWriteUsersIntoFile() {
//        userRepository.signUp(SIGNUP_USERNAME1, PASSWORD);
//        User logginUser1 = userRepository.login(SIGNUP_USERNAME1, PASSWORD);
//        userRepository.signUp(SIGNUP_USERNAME2, PASSWORD);
//        User logginUser2 = userRepository.login(SIGNUP_USERNAME2, PASSWORD);
//        userRepository.signUp(SIGNUP_USERNAME3, PASSWORD);
//        User logginUser3 = userRepository.login(SIGNUP_USERNAME3, PASSWORD);
//
//        userRepository.writeUsersInFile();
//
//        userRepository.populateUsersList();
//        assertEquals(3, userRepository.getSize());
//
//    }

    @Test
    public void testLoginWithLoadedData() {
        userRepository.populateUsersList();
        String username = "test2";
        String password = "Test@1234";

        User loggedInUser = userRepository.login(username, password);
        assertEquals("test2", loggedInUser.getUsername());
    }
}