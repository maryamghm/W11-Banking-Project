package org.example.repository;

import lombok.SneakyThrows;
import org.example.domain.Account;
import org.example.domain.AccountPlan;
import org.example.domain.AccountType;
import org.example.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

public class AccountRepositoryTest {

    private AccountRepository accountRepository;
    private UserRepository userRepository;

    @BeforeEach
    @SneakyThrows
    public void setup() {
        URI filePath = getClass().getClassLoader().getResource("accounts.csv").toURI();
        accountRepository = new AccountRepository(new File(filePath));
        URI usersFilePath = getClass().getClassLoader().getResource("users.csv").toURI();
        userRepository = new UserRepository(new File(usersFilePath));
    }

    @Test
    public void testLoadingFile() {
        assertEquals(2, accountRepository.getSize());
    }

    @Test
    public void addAccount() {
        String username = "test_user3";
        String password = "Test@1234";
        User newUser = userRepository.signUp(username, password);
        AccountType type = AccountType.CHECKING_ACCOUNT;
        AccountPlan plan = AccountPlan.NORMAL;
        double withdrawLimit = 100.0;
        double balance = 1000;
        String pin = "1234";

        Account account = accountRepository.addNewAccount(newUser.getId(), pin, type, plan, withdrawLimit, balance);

        assertNotNull(account);
        assertEquals(type, account.getType());
        assertEquals(type, account.getType());
        assertEquals(newUser.getId(), account.getUserId());
        assertEquals(withdrawLimit, account.getWithdrawLimit());
        assertEquals(balance, account.getBalance());
    }

    @Test
    public void showBalancetest() {
        String username = "test_user3";
        String password = "Test@1234";
        User newUser = userRepository.signUp(username, password);
        AccountType type = AccountType.CHECKING_ACCOUNT;
        AccountPlan plan = AccountPlan.NORMAL;
        double withdrawLimit = 100.0;
        double balance = 1000;
        String pin = "1234";

        Account account = accountRepository.addNewAccount(newUser.getId(), pin, type, plan, withdrawLimit, balance);
        assertEquals(balance, account.getBalance());
    }

    @Test
    public void validateDepositLimitTest() {
        double amount = 400.0;
        AccountPlan accountPlan = AccountPlan.NORMAL;

        assertThrows(IllegalArgumentException.class, () -> accountRepository.validateDepositLimit(amount, accountPlan));

    }

    @Test
    public void depositTest() {
        String username = "test_user3";
        String password = "Test@1234";
        User newUser = userRepository.signUp(username, password);
        AccountType type = AccountType.CHECKING_ACCOUNT;
        AccountPlan plan = AccountPlan.NORMAL;
        double withdrawLimit = 100.0;
        double balance = 1000;
        String pin = "1234";

        Account account = accountRepository.addNewAccount(newUser.getId(), pin, type, plan, withdrawLimit, balance);
        double amount = 100.0;
        account.deposit(amount);
        assertEquals((balance + amount), account.getBalance());
    }

    @Test
    public void withdrawTest() {
        String username = "test_user3";
        String password = "Test@1234";
        User newUser = userRepository.signUp(username, password);
        AccountType type = AccountType.CHECKING_ACCOUNT;
        AccountPlan plan = AccountPlan.NORMAL;
        double withdrawLimit = 100.0;
        double balance = 1000;
        String pin = "1234";

        Account account = accountRepository.addNewAccount(newUser.getId(), pin, type, plan, withdrawLimit, balance);

        double amount = 100.0;
        account.withdraw(amount);
        assertEquals((balance - amount), account.getBalance());
    }

    @Test
    public void overdraftCheckingAccountTest() {
        String username = "test_user3";
        String password = "Test@1234";
        User newUser = userRepository.signUp(username, password);
        AccountType type = AccountType.CHECKING_ACCOUNT;
        AccountPlan plan = AccountPlan.NORMAL;
        double withdrawLimit = 100.0;
        double balance = 100;
        String pin = "1234";
        double amount = 100;
        double penalty = 50.0;

        Account account = accountRepository.addNewAccount(newUser.getId(), pin, type, plan, withdrawLimit, balance);

        account.withdraw(amount);
        assertEquals((balance - amount), account.getBalance());

        balance = account.getBalance();
        account.withdraw(amount);
        assertEquals((balance - (amount + penalty)), account.getBalance());

        balance = account.getBalance();
        account.withdraw(amount);
        assertEquals((balance - (amount + penalty)), account.getBalance());

        assertThrows(IllegalArgumentException.class, () -> account.withdraw(amount));
    }

    @Test
    public void overdraftSavingsAccountTest() {
        String username = "test_user3";
        String password = "Test@1234";
        User newUser = userRepository.signUp(username, password);
        AccountType type = AccountType.SAVINGS_ACCOUNT;
        AccountPlan plan = AccountPlan.NORMAL;
        double withdrawLimit = 100.0;
        double balance = 100;
        String pin = "1234";
        double amount = 100;

        Account account = accountRepository.addNewAccount(newUser.getId(), pin, type, plan, withdrawLimit, balance);
        account.withdraw(amount);

        assertThrows(IllegalArgumentException.class, () -> account.withdraw(amount));
    }

    @Test
    public void transferTest() {
        String username = "test_user3";
        String password = "Test@1234";
        User newUser = userRepository.signUp(username, password);
        AccountType type = AccountType.SAVINGS_ACCOUNT;
        AccountPlan plan = AccountPlan.NORMAL;
        double withdrawLimit = 170.0;
        double balance1 = 500.0;
        String pin = "1234";

        Account account1 = accountRepository.addNewAccount(newUser.getId(), pin, type, plan, withdrawLimit, balance1);

        username = "test_user4";
        password = "Test@1234";
        User newUser2 = userRepository.signUp(username, password);
        type = AccountType.CHECKING_ACCOUNT;
        plan = AccountPlan.SILVER;
        withdrawLimit = accountRepository.getWithdrawLimit(plan);
        double balance2 = 1000.0;
        pin = "1234";

        Account account2 = accountRepository.addNewAccount(newUser.getId(), pin, type, plan, withdrawLimit, balance2);
        Double amount = 150.0;
        accountRepository.transfer(account1.getAccountNumber(), account2.getAccountNumber(), amount);
        assertEquals((balance1 - amount), account1.getBalance());
        assertEquals((balance2 + amount), account2.getBalance());

        Double amount2 = 300.0;
        assertThrows(IllegalArgumentException.class, () -> accountRepository.transfer(account1.getAccountNumber(), account2.getAccountNumber(), amount2));

    }
}
