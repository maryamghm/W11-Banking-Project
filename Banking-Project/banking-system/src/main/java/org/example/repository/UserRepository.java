package org.example.repository;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.example.domain.User;
import org.example.domain.UserType;
import org.example.exception.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRepository {
    private final Map<String, User> users = new HashMap<>();
    private int idCounter;
    private final File dataSource;
    private final int MAX_LOGIN_ATTEMPTS = 3;

    private static UserRepository userRepositoryInstance = null;

    private UserRepository(File dataSource) {
        this.dataSource = dataSource;
        populateUsersList();
    }

    public static UserRepository getInstance(File datasource) {
        if (userRepositoryInstance == null) {
            return userRepositoryInstance = new UserRepository(datasource);
        }
        return userRepositoryInstance;
    }

    public int getSize() {
        return users.size();
    }

    public void populateUsersList() {
        CsvMapper csvMapper = new CsvMapper();
        CsvSchema schema = csvMapper.schemaFor(User.class)
                .withHeader()
                .withColumnReordering(true);
        ObjectReader reader = csvMapper.readerFor(User.class).with(schema);

        try {
            List<Object> userList = reader.readValues(dataSource).readAll();
            for (Object obj : userList) {
                if (obj instanceof User) {
                    users.put(((User) obj).getUsername(), (User) obj);
                }
            }

            idCounter = users.values().stream()
                    .map(User::getId)
                    .max(Integer::compareTo)
                    .orElse(0);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeUsersInFile() {
        CsvMapper csvMapper = new CsvMapper();
        CsvSchema schema = CsvSchema.builder()
                .addColumn("id")
                .addColumn("username")
                .addColumn("password")
                .addColumn("type")
                .addColumn("isActive")
                .setUseHeader(true)
                .build();
        try {
            csvMapper.writer(schema).writeValue(dataSource, users.values());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public User signUp(String username, String password) {
        User user = createNewUser(username, password);
        users.put(user.getUsername(), user);
        System.out.println("Welcome " + username);
        return user;
    }

    public User createNewUser(String username, String password) {
        validateUsername(username);
        validatePassword(password);
        User user = new User(++idCounter, username, password);
        user.setType(UserType.CUSTOMER);
        user.setActive(true);
        return user;
    }

    public void validateUsername(String username) {
        if (username == null || username.isBlank() || !username.matches("^[^A-Z]*$")
                || users.containsKey(username))
            throw new InvalidUserNameException("Username is not valid.");
    }

    public void validatePassword(String password) {
        if (!User.isValidPassword(password))
            throw new InvalidPasswordException("Password is not valid.");
    }



    public User login(String username, String password) {
        if (users.containsKey(username)) {
            User user = users.get(username);
            if (!user.isActive()) {
                throw new LoginFailedException("This user is already deactivated!");
            }
            if (user.getLoginFailedAttempts() < MAX_LOGIN_ATTEMPTS) {
                if (user.matchPassword(password)) {
                    user.setLoginFailedAttempts(0);
                    return user;
                }
                user.setLoginFailedAttempts(user.getLoginFailedAttempts() + 1);
                throw new LoginFailedException("Login failed.");
            }
            throw new UserLockedException();
        }
        throw new LoginFailedException("Username doesn't exist!");
    }

    public void logout(String username) {
        if (!users.containsKey(username)) {
            throw new LogoutFailedException("Invalid username.");
        }
    }

    public void resetPassword(String username, String oldPassword, String newPassword) {
        if (!users.containsKey(username)) {
            throw new IllegalArgumentException("User not found.");
        }
        User user = users.get(username);
        if (!user.matchPassword(oldPassword)) {
            throw new IllegalArgumentException("Old password doesn't match!");
        }
        user.setPassword(newPassword);
    }

    public String getUserInfo(int userId) {
        return "name";
    }

    public void deactivateUser(User user) {
        if (!users.containsKey(user.getUsername())) {
            throw new IllegalArgumentException("Invalid User.");
        }
        user.setActive(false);
    }
}
