package org.example.domain;

public class CheckingAccount extends Account {

    private int overdraftCounter;
    private static final int OVERDRAFT_LIMIT = 2;

    public CheckingAccount() {
        this.overdraftCounter = 0;
    }

    @Override
    public void withdraw(double amount) {
        if (amount > this.getWithdrawLimit()) {
            throw new IllegalArgumentException("Withdraw limit exceeds.");
        }


    }

}
