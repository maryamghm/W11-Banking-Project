package my.bank.repository;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.SneakyThrows;
import my.bank.domain.Account;
import my.bank.domain.Transaction;
import my.bank.domain.TransactionType;
import my.bank.utils.CsvMapperUtils;

import java.io.File;
import java.time.LocalDate;
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

    private static TransactionRepository transactionRepositoryInstance = null;

    private TransactionRepository(File dataSource) {
        this.dataSource = dataSource;
        this.transactions = new ArrayList<>(loadTransactions());
    }

    void clear() {
        transactions.clear();
    }

    public static TransactionRepository getInstance(File dataSource) {
        if (transactionRepositoryInstance == null) {
            return transactionRepositoryInstance = new TransactionRepository(dataSource);
        }
        return transactionRepositoryInstance;
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

    @SneakyThrows
    public List<Transaction> getTransactions(Account account, LocalDate fromDate, LocalDate toDate) {
        LocalDateTime fromDateTime = fromDate.atStartOfDay();
        LocalDateTime toDateTime = toDate.atStartOfDay();
        return getTransactions(account)
                .stream()
                .filter(transaction -> transaction.getTimestamp().isAfter(fromDateTime) && transaction.getTimestamp().isBefore(toDateTime))
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

    int getSize() {
        return transactions.size();
    }
}
