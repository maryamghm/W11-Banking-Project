package my.bank.exception;

public class UserLockedException extends RuntimeException {
    public UserLockedException() {
        super("User is locked due to maximum attempt without success reached");
    }
}
