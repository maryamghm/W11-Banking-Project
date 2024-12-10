package repository;

import lombok.SneakyThrows;
import org.example.domain.User;
import org.example.domain.UserType;
import org.example.exception.LoginFailedException;
import org.example.exception.SignupFailedException;
import org.example.exception.UserLockedException;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {

    private UserRepository userRepository;

    @BeforeEach
    @SneakyThrows
    public void setup() {
        URI filePath = getClass().getClassLoader().getResource("users.csv").toURI();
        userRepository = new UserRepository(new File(filePath));
    }

    @Test
    public void testLoadingFile() {
        assertEquals(2, userRepository.getSize());
    }

    @Test
    public void testSignup() {
        String username = "test_user1";
        String password = "Test@1234";
        User newUser = userRepository.signUp(username, password);

        assertNotNull(newUser);
        assertNotNull(newUser.getId());
        assertEquals(username, newUser.getUsername());
        assertNotEquals(password, newUser.getPassword());
        assertEquals(UserType.CUSTOMER, newUser.getType());

        User loggedInUser = userRepository.login(username, password);
        assertEquals(newUser, loggedInUser);
    }

    @Test
    public void testDoubleSignup() {
        String username = "test_user1";
        String password = "Test@1234";
        User newUser = userRepository.signUp(username, password);
        assertNotNull(newUser);

        assertThrows(SignupFailedException.class, () -> userRepository.signUp(username, password));
    }

    @Test
    public void testSignupWithUppercaseUsername() {
        String username = "testUser1";
        String password = "Test@1234";

        assertThrows(SignupFailedException.class, () -> userRepository.signUp(username, password));
    }

    @Test
    public void testLoginWithWrongPassword() {
        String username = "test_user2";
        String password = "Test@1234";
        User newUser = userRepository.signUp(username, password);

        String wrongPassword = password.toLowerCase();
        assertThrows(LoginFailedException.class, () -> userRepository.login(username, wrongPassword));
    }

    @Test
    public void testLoginLimit() {
        String username = "test_user2";
        String password = "Test@1234";
        User newUser = userRepository.signUp(username, password);

        String wrongPassword = password.toLowerCase();
        for (int i = 0; i < 3; i++) {
            assertThrows(LoginFailedException.class, () -> userRepository.login(username, wrongPassword));
        }

        assertThrows(UserLockedException.class, () -> userRepository.login(username, wrongPassword));
    }

    @Test
    public void testResetPassword() {
        String username = "test_user2";
        String password = "Test@1234";
        userRepository.signUp(username, password);
        User logginUser = userRepository.login(username, password);
        String newPassword = "Test@12345";
        userRepository.resetPassword(username, password, newPassword);

        assertTrue(logginUser.matchPassword(newPassword));
    }

}