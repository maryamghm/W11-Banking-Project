package org.example.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Data
public class User {
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;

    @JsonProperty("type")
    private UserType type;

    @JsonProperty("isActive")
    private boolean isActive;

    @JsonIgnore
    private int loginFailedAttempts = 0;

    @JsonIgnore
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User() {
    }

    public User(int id, String username, String password) {
        this.id = id;
        this.username = username;
        setRawPassword(password);
    }

    public void setRawPassword(String password) {
        if (!isValidPassword(password)) {
            throw new IllegalArgumentException("Password does not meet the required criteria.");
        }
        this.password = PasswordHashing.hashPasswordSHA1(password);
    }

    public boolean matchPassword(String password) {
        return (PasswordHashing.hashPasswordSHA1(password).equals(this.password));
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            System.out.println("Password must be at least 6 characters long.");
            return false;
        }
        if (!password.matches(".*[a-z].*")) {
            System.out.println("Password must contain at least one lowercase letter.");
            return false;
        }
        if (!password.matches(".*[A-Z].*")) {
            System.out.println("Password must contain at least one uppercase letter.");
            return false;
        }
        if (!password.matches(".*\\d.*")) {
            System.out.println("Password must contain at least one digit.");
            return false;
        }
        return true;
    }
}