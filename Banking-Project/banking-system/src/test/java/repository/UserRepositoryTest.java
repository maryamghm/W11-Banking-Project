package repository;

import lombok.SneakyThrows;
import org.example.domain.User;
import org.example.domain.UserType;
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
        assertEquals(1, userRepository.getSize());
    }

    @Test
    public void testSignup() {
        String username = "testUser1";
        String password = "Test@1234";
        User newUser = userRepository.signUp(username, password);

        assertNotNull(newUser);
        assertNotNull(newUser.getId());
        assertEquals(username, newUser.getUsername());
        assertEquals(password, newUser.getPassword());
        assertEquals(UserType.CUSTOMER, newUser.getType());

        User loggedInUser = userRepository.login(username, password);
        assertEquals(newUser, loggedInUser);
    }

    @Test
    public void testDoubleSignup() {
        String username = "testUser1";
        String password = "Test@1234";
        User newUser = userRepository.signUp(username, password);
        assertNotNull(newUser);

        assertThrows(IllegalArgumentException.class, () -> userRepository.signUp(username, password));
    }

}