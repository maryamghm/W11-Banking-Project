package repository;

import lombok.SneakyThrows;
import org.example.domain.User;
import org.example.domain.UserType;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserRepositoryTest {
    @SneakyThrows
    @Test
    public void testLoadingFile() {
        URI filePath = getClass().getClassLoader().getResource("users.csv").toURI();
        UserRepository userRepository = new UserRepository(new File(filePath));

        assertEquals(1, userRepository.getSize());
    }

    @Test
    public void testSignup() throws URISyntaxException {
        URI filePath = getClass().getClassLoader().getResource("users.csv").toURI();
        UserRepository userRepository = new UserRepository(new File(filePath));

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
}