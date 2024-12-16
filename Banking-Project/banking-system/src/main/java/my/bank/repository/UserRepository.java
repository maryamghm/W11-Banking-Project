package my.bank.repository;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import my.bank.domain.User;
import my.bank.domain.UserType;
import my.bank.exception.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UserRepository {
    private final Map<String, User> userNameToUserMap = new HashMap<>();
    private final Map<Integer, User> userIdToUserMap = new HashMap<>();
    private int idCounter;
    private final File dataSource;
    private static final int MAX_LOGIN_ATTEMPTS = 3;

    private static UserRepository userRepositoryInstance = null;

    private UserRepository(File dataSource) {
        this.dataSource = dataSource;
        populateUsersList();
    }

    void clear() {
        userNameToUserMap.clear();
        userIdToUserMap.clear();
    }

    public static UserRepository getInstance(File datasource) {
        return Objects.requireNonNullElseGet(userRepositoryInstance, () -> userRepositoryInstance = new UserRepository(datasource));
    }

    public int getSize() {
        return userNameToUserMap.size();
    }

    public void populateUsersList() {
        CsvMapper csvMapper = new CsvMapper();
        CsvSchema schema = csvMapper.schemaFor(User.class)
                .withHeader()
                .withColumnReordering(true);
        ObjectReader reader = csvMapper.readerFor(User.class).with(schema);

        try {
            reader.readValues(dataSource).readAll().stream().map(o -> (User) o)
                    .forEach(user -> {
                        userNameToUserMap.put(user.getUsername(), user);
                        userIdToUserMap.put(user.getId(), user);
                    });

            idCounter = userIdToUserMap.keySet().stream().max(Integer::compareTo).orElse(0);

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
                .addColumn("firstName")
                .addColumn("lastName")
                .addColumn("type")
                .addColumn("isActive")
                .setUseHeader(true)
                .build();
        try {
            csvMapper.writer(schema).writeValue(dataSource, userNameToUserMap.values());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public User signUp(String username, String password, String firstName, String lastName) {
        User user = createNewUser(username, password, firstName, lastName);
        userNameToUserMap.put(user.getUsername(), user);
        return user;
    }

    public User createNewUser(String username, String password, String firstName, String lastName) {
        validateUsername(username, true);
        validatePassword(password);
        validateFirstName(firstName);
        validateLastName(lastName);
        User user = new User(++idCounter, username, password, firstName, lastName);
        user.setType(UserType.CUSTOMER);
        user.setActive(true);
        return user;
    }

    public void validateUsername(String username, boolean isNewUser) {
        if (isNewUser && userNameToUserMap.containsKey(username)) {
            throw new SignupFailedException("Username is already exists");
        }
        if (username == null || username.isBlank() || !username.matches("^[^A-Z]*$")) {
            throw new InvalidUserNameException("Username is not valid. It shoudn't contain any Uppercase letters.");
        }
    }

    public void validatePassword(String password) {
        if (!User.isValidPassword(password))
            throw new InvalidPasswordException("Password is not valid.");
    }


    public void validateFirstName(String firstName) {
        if (firstName == null || firstName.isBlank()) {
            throw new InvalidPasswordException("First Name cannot be blank.");
        }
    }

    public void validateLastName(String lastName) {
        if (lastName == null || lastName.isBlank()) {
            throw new InvalidPasswordException("Last Name cannot be blank.");
        }
    }

    public User login(String username, String password) {
        if (userNameToUserMap.containsKey(username)) {
            User user = userNameToUserMap.get(username);
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
        if (!userNameToUserMap.containsKey(username)) {
            throw new LogoutFailedException("Invalid username.");
        }
    }

    public void resetPassword(String username, String oldPassword, String newPassword) {
        if (!userNameToUserMap.containsKey(username)) {
            throw new IllegalArgumentException("User not found.");
        }
        User user = userNameToUserMap.get(username);
        if (!user.matchPassword(oldPassword)) {
            throw new IllegalArgumentException("Old password doesn't match!");
        }
        user.setRawPassword(newPassword);
    }

    public String getUserInfo(int userId) {
        if (!userIdToUserMap.containsKey(userId)) {
            throw new IllegalArgumentException("UserId not found.");
        }
        return userIdToUserMap.get(userId).getFirstName() + " " + userIdToUserMap.get(userId).getLastName();
    }

    public void deactivateUser(User user) {
        if (!userNameToUserMap.containsKey(user.getUsername())) {
            throw new IllegalArgumentException("Invalid User.");
        }
        user.setActive(false);
    }

}
