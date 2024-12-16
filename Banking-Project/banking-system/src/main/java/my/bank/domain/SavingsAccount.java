package my.bank.domain;

import static my.bank.domain.AccountType.SAVINGS_ACCOUNT;

public class SavingsAccount extends Account {
    public SavingsAccount() {
        setType(SAVINGS_ACCOUNT);
        this.setOverdraftCounter(0);
    }

    private double interestRate;
    @Override
    public void withdraw(double amount) {
        if (amount > this.getWithdrawLimit()) {
            throw new IllegalArgumentException("Withdraw limit exceeds.");
        }
        if (this.getBalance() - amount < 0.0) {
            throw new IllegalArgumentException("You cannot withdraw an amount more than your current balance.");
        }
        this.setBalance(this.getBalance() - amount);
    }
}
