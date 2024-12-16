package my.bank.repository;

import lombok.SneakyThrows;
import my.bank.domain.Account;
import my.bank.domain.AccountPlan;
import my.bank.domain.AccountType;
import my.bank.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;

import static my.bank.TestFixtures.*;
import static org.junit.jupiter.api.Assertions.*;

public class AccountRepositoryTest {

    private AccountRepository accountRepository;
    private UserRepository userRepository;

    @BeforeEach
    @SneakyThrows
    public void setup() {
        URI filePath = getClass().getClassLoader().getResource("accounts.csv").toURI();
        accountRepository = AccountRepository.getInstance(new File(filePath));
        URI usersFilePath = getClass().getClassLoader().getResource("users.csv").toURI();
        userRepository = UserRepository.getInstance(new File(usersFilePath));
    }

    @AfterEach
    public void clearLists() {
        accountRepository.clear();
        userRepository.clear();
    }

    @Test
    public void testLoadingFile() {
        assertEquals(0, accountRepository.getSize());
    }

    @Test
    public void addAccount() {
        User user = userRepository.signUp(SIGNUP_USERNAME1, PASSWORD, FIRSTNAME, LASTNAME);
        AccountType type = AccountType.CHECKING_ACCOUNT;
        AccountPlan plan = AccountPlan.NORMAL;
        double withdrawLimit = 100.0;
        double balance = 1000;
        String pin = "1234";

        Account account = accountRepository.addNewAccount(user.getId(), pin, type, plan, withdrawLimit, balance);
        assertDoesNotThrow(() -> account.validatePin(pin));
        assertNotNull(account);
        assertEquals(type, account.getType());
        assertEquals(plan, account.getPlan());
        assertEquals(user.getId(), account.getUserId());
        assertEquals(withdrawLimit, account.getWithdrawLimit());
        assertEquals(balance, account.getBalance());

        accountRepository.writeAccountsIntoFile();
    }

    @Test
    public void showBalanceTest() {
        User user = userRepository.signUp(SIGNUP_USERNAME1, PASSWORD, FIRSTNAME, LASTNAME);
        AccountType type = AccountType.CHECKING_ACCOUNT;
        AccountPlan plan = AccountPlan.NORMAL;
        double withdrawLimit = 100.0;
        double balance = 1000;
        String pin = "1234";

        Account account = accountRepository.addNewAccount(user.getId(), pin, type, plan, withdrawLimit, balance);
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
        User user = userRepository.signUp(SIGNUP_USERNAME1, PASSWORD, FIRSTNAME, LASTNAME);
        AccountType type = AccountType.CHECKING_ACCOUNT;
        AccountPlan plan = AccountPlan.NORMAL;
        double withdrawLimit = 100.0;
        double balance = 1000;
        String pin = "1234";

        Account account = accountRepository.addNewAccount(user.getId(), pin, type, plan, withdrawLimit, balance);
        double amount = 100.0;
        account.deposit(amount);
        assertEquals((balance + amount), account.getBalance());
    }

    @Test
    public void withdrawTest() {
        User user = userRepository.signUp(SIGNUP_USERNAME1, PASSWORD, FIRSTNAME, LASTNAME);
        AccountType type = AccountType.CHECKING_ACCOUNT;
        AccountPlan plan = AccountPlan.NORMAL;
        double withdrawLimit = 100.0;
        double balance = 1000;
        String pin = "1234";

        Account account = accountRepository.addNewAccount(user.getId(), pin, type, plan, withdrawLimit, balance);

        double amount = 100.0;
        account.withdraw(amount);
        assertEquals((balance - amount), account.getBalance());
    }

    @Test
    public void overdraftCheckingAccountTest() {
        User user = userRepository.signUp(SIGNUP_USERNAME1, PASSWORD, FIRSTNAME, LASTNAME);
        AccountType type = AccountType.CHECKING_ACCOUNT;
        AccountPlan plan = AccountPlan.NORMAL;
        double withdrawLimit = 100.0;
        double balance = 100;
        String pin = "1234";
        double amount = 100;
        double penalty = 50.0;

        Account account = accountRepository.addNewAccount(user.getId(), pin, type, plan, withdrawLimit, balance);

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
        User user = userRepository.signUp(SIGNUP_USERNAME1, PASSWORD, FIRSTNAME, LASTNAME);
        AccountType type = AccountType.SAVINGS_ACCOUNT;
        AccountPlan plan = AccountPlan.NORMAL;
        double withdrawLimit = 100.0;
        double balance = 100;
        String pin = "1234";
        double amount = 100;

        Account account = accountRepository.addNewAccount(user.getId(), pin, type, plan, withdrawLimit, balance);
        account.withdraw(amount);

        assertThrows(IllegalArgumentException.class, () -> account.withdraw(amount));
    }

    @Test
    public void transferTest() {
        User user = userRepository.signUp(SIGNUP_USERNAME1, PASSWORD, FIRSTNAME, LASTNAME);
        AccountType type = AccountType.SAVINGS_ACCOUNT;
        AccountPlan plan = AccountPlan.NORMAL;
        double withdrawLimit = 170.0;
        double balance1 = 500.0;
        String pin = "1234";

        Account account1 = accountRepository.addNewAccount(user.getId(), pin, type, plan, withdrawLimit, balance1);

        User user2 = userRepository.signUp(SIGNUP_USERNAME2, PASSWORD, FIRSTNAME, LASTNAME);
        type = AccountType.CHECKING_ACCOUNT;
        plan = AccountPlan.SILVER;
        withdrawLimit = accountRepository.getWithdrawLimit(plan);
        double balance2 = 1000.0;
        pin = "1234";

        Account account2 = accountRepository.addNewAccount(user.getId(), pin, type, plan, withdrawLimit, balance2);
        Double amount = 150.0;
        accountRepository.transfer(account1.getAccountNumber(), account2.getAccountNumber(), amount);
        assertEquals((balance1 - amount), account1.getBalance());
        assertEquals((balance2 + amount), account2.getBalance());

        Double amount2 = 300.0;
        assertThrows(IllegalArgumentException.class, () -> accountRepository.transfer(account1.getAccountNumber(), account2.getAccountNumber(), amount2));

    }

    @Test
    public void deactivateAccountTest() {
        User user = userRepository.signUp(SIGNUP_USERNAME1, PASSWORD, FIRSTNAME, LASTNAME);
        AccountType type = AccountType.SAVINGS_ACCOUNT;
        AccountPlan plan = AccountPlan.NORMAL;
        double withdrawLimit = 170.0;
        double balance1 = 500.0;
        String pin = "1234";

        Account account = accountRepository.addNewAccount(user.getId(), pin, type, plan, withdrawLimit, balance1);

        accountRepository.deactivateAccount(account);
        userRepository.deactivateUser(user);
        assertEquals(false, account.isActive());
        assertEquals(false, user.isActive());

    }
}
