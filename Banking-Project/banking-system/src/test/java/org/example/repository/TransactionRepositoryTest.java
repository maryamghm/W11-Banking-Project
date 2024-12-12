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
import java.util.List;

class TransactionRepositoryTest {

    private TransactionRepository transactionRepository;
    private Account account;

    @BeforeEach
    @SneakyThrows
    public void setup() {
        URI filePath = getClass().getClassLoader().getResource("transactions.csv").toURI();
        transactionRepository = new TransactionRepository(new File(filePath));
        account = new CheckingAccount();
        account.setAccountNumber(1);
    }

    @Test
    void readTransactionsFromFile() {
        transactionRepository.addTransaction(account, TransactionType.DEBIT, 50);
        List<Transaction> transactions = transactionRepository.getTransactions(account);
        Assertions.assertEquals(1, transactions.size());
    }

    @Test
    void addTransaction() {
        transactionRepository.addTransaction(account, TransactionType.CREDIT, 100);
        transactionRepository.addTransaction(account, TransactionType.DEBIT, 50);
        List<Transaction> transactions = transactionRepository.getTransactions(account);
        Assertions.assertEquals(2, transactions.size());
    }
}