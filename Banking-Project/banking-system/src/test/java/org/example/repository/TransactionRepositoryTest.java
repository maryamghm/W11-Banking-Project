package org.example.repository;

import lombok.SneakyThrows;
import org.example.domain.Account;
import org.example.domain.CheckingAccount;
import org.example.domain.Transaction;
import org.example.domain.TransactionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;

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

    @Test
    public void readTransactionsFromFile() {
        transactionRepository.addTransaction(account, TransactionType.DEBIT, 50);
        List<Transaction> transactions = transactionRepository.getTransactions(account);
        Assertions.assertEquals(1, transactions.size());
    }

    @Test
    public void addTransaction() {

    }

    @Test
    public void testGetTransactionsByAccount() {
        Account account = new CheckingAccount();
        account.setAccountNumber(2);
        List<Transaction> transactions = transactionRepository.getTransactions(account);
        Assertions.assertEquals(4, transactions.size());
    }

    @Test
    public void testGetTransactionsByAccountAndDate() {
        Account account = new CheckingAccount();
        account.setAccountNumber(2);
        LocalDate date1 = LocalDate.parse("2024-10-01");
        LocalDate date2 = LocalDate.parse("2024-12-12");

        List<Transaction> transactions = transactionRepository.getTransactions(account, date1, date2);
        Assertions.assertEquals(2, transactions.size());
    }
}