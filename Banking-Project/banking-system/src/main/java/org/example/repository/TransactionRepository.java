package org.example.repository;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.SneakyThrows;
import org.example.domain.Account;
import org.example.domain.Transaction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TransactionRepository {
    private final File dataSource;
    private List<Transaction> transactions = new ArrayList<>();

    public TransactionRepository(File dataSource) {
        this.dataSource = dataSource;
    }

    @SneakyThrows
    private List<Transaction> getTransactions(Account account) {
        CsvMapper csvMapper = new CsvMapper();
        CsvSchema schema = csvMapper.schemaFor(Transaction.class)
                .withHeader()
                .withColumnReordering(true);

        ObjectReader reader = csvMapper.readerFor(Transaction.class).with(schema);

        return reader.readValues(dataSource).readAll()
                .stream().filter(o -> o instanceof Transaction)
                .map(o -> (Transaction) o)
                .filter(transaction -> transaction.getAccountNumber().equals(account.getAccountNumber()))
                .toList();
    }
}
