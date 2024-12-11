package repository;

import lombok.SneakyThrows;
import org.example.domain.Account;
import org.example.domain.AccountPlan;
import org.example.domain.AccountType;
import org.example.domain.User;
import org.example.repository.AccountRepository;
import org.example.repository.UserRepository;
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

        Account newAccount = accountRepository.addNewAccount(newUser.getId(), pin, type, plan, withdrawLimit, balance);

        assertNotNull(newAccount);
        assertEquals(type, newAccount.getType());
        assertEquals(type, newAccount.getType());
        assertEquals(newUser.getId(), newAccount.getUserId());
        assertEquals(withdrawLimit, newAccount.getWithdrawLimit());
        assertEquals(balance, newAccount.getBalance());
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

        Account newAccount = accountRepository.addNewAccount(newUser.getId(), pin, type, plan, withdrawLimit, balance);
        assertEquals(balance, newAccount.getBalance());
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

        Account newAccount = accountRepository.addNewAccount(newUser.getId(), pin, type, plan, withdrawLimit, balance);
        double amount = 100.0;
        newAccount.deposit(amount);
        assertEquals((balance + amount), newAccount.getBalance());
    }

}
