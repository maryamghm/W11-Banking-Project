package my.bank.repository;

import lombok.SneakyThrows;
import my.bank.domain.Account;
import my.bank.domain.CheckingAccount;
import my.bank.domain.Transaction;
import my.bank.domain.TransactionType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransactionRepositoryTest {

    private TransactionRepository transactionRepository;
    private UserRepository userRepository;
    private AccountRepository accountRepository;
    private Account account;

    @BeforeEach
    @SneakyThrows
    public void setup() {
        URI filePath1 = getClass().getClassLoader().getResource("transactions.csv").toURI();
        transactionRepository = TransactionRepository.getInstance(new File(filePath1));
        URI filePath2 = getClass().getClassLoader().getResource("users.csv").toURI();
        userRepository = UserRepository.getInstance(new File(filePath2));
        URI filePath3 = getClass().getClassLoader().getResource("accounts.csv").toURI();
        accountRepository = AccountRepository.getInstance(new File(filePath3));
    }

    @AfterEach
    public void clearLists() {
        userRepository.clear();
        transactionRepository.clear();
        accountRepository.clear();
    }


    @Test
    public void testGetTransactionsByAccount() {
        account = new CheckingAccount();
        account.setAccountNumber(1);
        transactionRepository.addTransaction(account, TransactionType.DEBIT, 50);
        List<Transaction> transactions = transactionRepository.getTransactions(account);
        assertEquals(1, transactions.size());
    }

    @Test
    public void addTransaction() {
        transactionRepository.clear();
        account = new CheckingAccount();
        account.setAccountNumber(1);
        transactionRepository.addTransaction(account, TransactionType.DEBIT, 50);
        transactionRepository.addTransaction(account, TransactionType.CREDIT, 100);
        assertEquals(2, transactionRepository.getSize());
    }


//    @Test
//    public void testGetTransactionsByAccountAndDate() {
//        Account account = new CheckingAccount();
//        account.setAccountNumber(2);
//        LocalDate date1 = LocalDate.parse("2024-10-01");
//        LocalDate date2 = LocalDate.parse("2024-12-12");
//
//        List<Transaction> transactions = transactionRepository.getTransactions(account, date1, date2);
//        Assertions.assertEquals(2, transactions.size());
//    }
}