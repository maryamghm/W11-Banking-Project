package org.example.domain;

public class CheckingAccount extends Account {

    private int overdraftCounter;
    private static final int OVERDRAFT_LIMIT = 2;
    private static final double OVERDRAFT_PENALTY = 50.0;

    public CheckingAccount() {
        this.overdraftCounter = 0;
    }

    @Override
    public void withdraw(double amount) {
        if (amount > this.getWithdrawLimit()) {
            throw new IllegalArgumentException("Withdraw limit exceeds.");
        }
        if (this.getBalance() - amount < 0.0) {
            if (overdraftCounter == OVERDRAFT_LIMIT) {
                throw new IllegalArgumentException("You have reached the overdraft limit. you cannot withdraw from your account.");
            }
            overdraftCounter++;
            this.setBalance(this.getBalance() - (amount + OVERDRAFT_PENALTY));
            return;
        }
        this.setBalance(this.getBalance() - amount);
    }
}
