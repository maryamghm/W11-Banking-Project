package org.example.repository;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.example.domain.User;
import org.example.domain.UserType;
import org.example.exception.LoginFailedException;
import org.example.exception.LogoutFailedException;
import org.example.exception.SignupFailedException;
import org.example.exception.UserLockedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserRepository(File sourceFilePath) {
        this.dataSource = sourceFilePath;
        populateUsersList();
    }

    public int getSize() {
        return users.size();
    }

    private void populateUsersList() {
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
                    .max(Integer::compareTo).orElse(0);

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
                .setUseHeader(true)
                .build();
        try {
            csvMapper.writer(schema).writeValue(new File("users.csv"), users.values());
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

    private User createNewUser(String username, String password) {
        if (username == null || username.isBlank() || !username.matches("^[^A-Z]*$") || users.containsKey(username))
            throw new SignupFailedException("Username is not valid.");
        if (!User.isValidPassword(password))
            throw new SignupFailedException("Password is not valid.");
        User user = new User(++idCounter, username, password);
        user.setType(UserType.CUSTOMER);
        return user;
    }

    public User login(String username, String password) {
        if (users.containsKey(username)) {
            User user = users.get(username);
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
}
