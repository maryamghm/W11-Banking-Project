package org.example.repository;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.SneakyThrows;
import org.example.domain.Account;
import org.example.domain.Transaction;
import org.example.domain.TransactionType;
import org.example.utils.CsvMapperUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionRepository {
    private final File dataSource;
    private final List<Transaction> transactions;
    private final CsvSchema writerSchema = CsvSchema.builder()
            .addColumn("accountNumber")
            .addColumn("timeStamp")
            .addColumn("type")
            .addColumn("amount")
            .setUseHeader(true)
            .build();
    private final CsvSchema readerSchema = CsvMapperUtils.getInstance().schemaFor(Transaction.class)
            .withHeader()
            .withColumnReordering(true);

    public TransactionRepository(File dataSource) {
        this.dataSource = dataSource;
        this.transactions = new ArrayList<>(loadTransactions());
    }

    @SneakyThrows
    private List<Transaction> loadTransactions() {
        ObjectReader reader = CsvMapperUtils.getInstance()
                .reader(readerSchema.withHeader())
                .forType(Transaction.class);

        return reader.readValues(dataSource).readAll()
                .stream().filter(o -> o instanceof Transaction)
                .map(o -> (Transaction) o)
                .toList();
    }

    @SneakyThrows
    public List<Transaction> getTransactions(Account account) {
        return transactions.stream()
                .filter(transaction -> transaction.getAccountNumber().equals(account.getAccountNumber()))
                .toList();
    }

    public void addTransaction(Account account, TransactionType transactionType, double amount) {
        Transaction transaction = new Transaction();
        transaction.setAccountNumber(account.getAccountNumber());
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setType(transactionType);
        transaction.setAmount(amount);
        transactions.add(transaction);

    }

    @SneakyThrows
    public void saveTransactions() {
        CsvMapperUtils.getInstance()
                .writer(writerSchema)
                .writeValue(dataSource, transactions);
    }
}
