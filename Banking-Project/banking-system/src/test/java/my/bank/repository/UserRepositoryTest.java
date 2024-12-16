package my.bank.repository;

import lombok.SneakyThrows;
import my.bank.domain.User;
import my.bank.domain.UserType;
import my.bank.exception.InvalidUserNameException;
import my.bank.exception.LoginFailedException;
import my.bank.exception.SignupFailedException;
import my.bank.exception.UserLockedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;

import static my.bank.TestFixtures.*;
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
        userRepository.clear();
    }

    @Test
    public void testLoadingFile() {
        assertEquals(0, userRepository.getSize());
    }

    @Test
    public void testSignup() {
        User newUser = userRepository.signUp(SIGNUP_USERNAME1, PASSWORD, FIRSTNAME, LASTNAME);

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
        User newUser = userRepository.signUp(SIGNUP_USERNAME1, PASSWORD, FIRSTNAME, LASTNAME);
        assertNotNull(newUser);

        assertThrows(SignupFailedException.class, () -> userRepository.signUp(SIGNUP_USERNAME1, PASSWORD, FIRSTNAME, LASTNAME));
    }

    @Test
    public void testSignupWithUppercaseUsername() {
        assertThrows(InvalidUserNameException.class, () -> userRepository.signUp(SIGNUP_USERNAME1.toUpperCase(), PASSWORD, FIRSTNAME, LASTNAME));
    }

    @Test
    public void testLoginWithWrongPassword() {

        User newUser = userRepository.signUp(SIGNUP_USERNAME1, PASSWORD, FIRSTNAME, LASTNAME);

        String wrongPassword = PASSWORD.toLowerCase();
        assertThrows(LoginFailedException.class, () -> userRepository.login(SIGNUP_USERNAME1, wrongPassword));
    }

    @Test
    public void testLoginLimit() {
        User newUser = userRepository.signUp(SIGNUP_USERNAME1, PASSWORD, FIRSTNAME, LASTNAME);

        String wrongPassword = PASSWORD.toLowerCase();
        for (int i = 0; i < 3; i++) {
            assertThrows(LoginFailedException.class, () -> userRepository.login(SIGNUP_USERNAME1, wrongPassword));
        }

        assertThrows(UserLockedException.class, () -> userRepository.login(SIGNUP_USERNAME1, wrongPassword));
    }

    @Test
    public void testResetPassword() {
        userRepository.signUp(SIGNUP_USERNAME1, PASSWORD, FIRSTNAME, LASTNAME);
        User logginUser = userRepository.login(SIGNUP_USERNAME1, PASSWORD);
        String newPassword = PASSWORD + "5";
        userRepository.resetPassword(SIGNUP_USERNAME1, PASSWORD, newPassword);

        assertTrue(logginUser.matchPassword(newPassword));
    }

    @Test
    public void testLoginDeactivatedUser() {
        userRepository.signUp(SIGNUP_USERNAME1, PASSWORD, FIRSTNAME, LASTNAME);
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
}