package org.example.exception;

public class SignupFailedException extends RuntimeException {
    public SignupFailedException(String message) {
        super(message);
    }
}
