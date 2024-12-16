package my.bank.domain;

import static my.bank.domain.AccountType.CHECKING_ACCOUNT;

public class CheckingAccount extends Account {
    private static final int OVERDRAFT_LIMIT = 2;
    private static final double OVERDRAFT_PENALTY = 50.0;

    public CheckingAccount() {
        setType(CHECKING_ACCOUNT);
    }

    @Override
    public void withdraw(double amount) {
        if (amount > this.getWithdrawLimit()) {
            throw new IllegalArgumentException("Withdraw limit exceeds.");
        }
        if (this.getBalance() - amount < 0.0) {
            if (this.getOverdraftCounter() == OVERDRAFT_LIMIT) {
                throw new IllegalArgumentException("You have reached the overdraft limit. you cannot withdraw from your account.");
            }
            this.setOverdraftCounter(this.getOverdraftCounter() + 1);
            this.setBalance(this.getBalance() - (amount + OVERDRAFT_PENALTY));
            return;
        }
        this.setBalance(this.getBalance() - amount);
    }
}
