package org.example.exception;

public class LogoutFailedException extends RuntimeException {
    public LogoutFailedException(String message) {
        super(message);
    }
}
