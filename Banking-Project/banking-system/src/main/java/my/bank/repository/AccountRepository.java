package my.bank.repository;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.Data;
import my.bank.domain.*;
import my.bank.exception.InvalidPinException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Data
public class AccountRepository {
    private static final double WITHDRAW_LIMIT_PLATINUM = 20000.0;
    private static final double WITHDRAW_LIMIT_GOLD = 10000.0;
    private static final double WITHDRAW_LIMIT_SILVER = 1000.0;
    private static final double WITHDRAW_LIMIT_NORMAL = 200.0;

    private static final double DEPOSIT_LIMIT_PLATINUM = 30000.0;
    private static final double DEPOSIT_LIMIT_GOLD = 15000.0;
    private static final double DEPOSIT_LIMIT_SILVER = 1500.0;
    private static final double DEPOSIT_LIMIT_NORMAL = 300.0;
    private Map<Integer, Account> userAccountMap = new HashMap<>();
    private Map<Integer, Account> accountNumberMap = new HashMap<>();

    private static TransactionRepository transactionRepository = TransactionRepository.getInstance(new File("transactions.csv"));

    private int accountNumberCounter;
    private final File dataSource;
    private static AccountRepository accountRepositoryInstance = null;

    private AccountRepository(File dataSource) {
        this.dataSource = dataSource;
        populateAccounts();
    }

    public static AccountRepository getInstance(File dataSource) {
        return Objects.requireNonNullElseGet(accountRepositoryInstance, () -> accountRepositoryInstance = new AccountRepository(dataSource));
    }

    void clear() {
        userAccountMap.clear();
        accountNumberMap.clear();
    }

    private void populateAccounts() {
        CsvMapper csvMapper = new CsvMapper();
        CsvSchema schema = csvMapper.schemaFor(Account.class)
                .withHeader()
                .withColumnReordering(true);

        ObjectReader reader = csvMapper.readerFor(Account.class).with(schema);

        try {
            reader.readValues(dataSource).readAll().stream().map(o -> (Account) o)
                    .forEach(account -> {
                        userAccountMap.put(account.getUserId(), account);
                        accountNumberMap.put(account.getAccountNumber(), account);
                    });

            accountNumberCounter = accountNumberMap.keySet().stream().max(Integer::compareTo).orElse(10000);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public Account addNewAccount(Integer userId, String pin, AccountType type, AccountPlan plan, double withdrawLimit, double initialDeposit) {
        Account newAccount = null;
        switch (type) {
            case AccountType.CHECKING_ACCOUNT -> {
                newAccount = new CheckingAccount();
                newAccount.setOverdraftCounter(0);
            }
            case AccountType.SAVINGS_ACCOUNT -> {
                newAccount = new SavingsAccount();
            }
        }
        newAccount.setUserId(userId);
        newAccount.setNewPin(pin);
        newAccount.setAccountNumber(++accountNumberCounter);
        newAccount.setPlan(plan);
        newAccount.setType(type);
        newAccount.setWithdrawLimit(withdrawLimit);
        newAccount.setBalance(initialDeposit);
        newAccount.setDepositLimit(getDepositLimit(plan));
        newAccount.setActive(true);

        userAccountMap.put(userId, newAccount);
        accountNumberMap.put(newAccount.getAccountNumber(), newAccount);
        logUserTransaction(newAccount, TransactionType.CREDIT, newAccount.getBalance());
        return newAccount;
    }


    public void validateNormalWithdrawLimit(double withdrawLimit) {
        if (withdrawLimit > WITHDRAW_LIMIT_NORMAL) {
            throw new IllegalArgumentException("Withdraw limit for a normal user can be maximum " + WITHDRAW_LIMIT_NORMAL + "$.");
        }
    }

    public double getWithdrawLimit(AccountPlan accountPlan) {
        switch (accountPlan) {
            case PLATINUM -> {
                return WITHDRAW_LIMIT_PLATINUM;
            }

            case GOLD -> {
                return WITHDRAW_LIMIT_GOLD;
            }

            case SILVER -> {
                return WITHDRAW_LIMIT_SILVER;
            }

            case NORMAL -> {
                return WITHDRAW_LIMIT_NORMAL;
            }
        }
        throw new IllegalArgumentException("Invalid Account Plan.");
    }

    public double getDepositLimit(AccountPlan accountPlan) {
        switch (accountPlan) {
            case PLATINUM -> {
                return DEPOSIT_LIMIT_PLATINUM;
            }

            case GOLD -> {
                return DEPOSIT_LIMIT_GOLD;
            }

            case SILVER -> {
                return DEPOSIT_LIMIT_SILVER;
            }

            case NORMAL -> {
                return DEPOSIT_LIMIT_NORMAL;
            }
        }
        throw new IllegalArgumentException("Invalid Account Plan.");
    }

    public void validateDepositLimit(double amount, AccountPlan accountPlan) {
        if (amount == 0.0) {
            throw new IllegalArgumentException("Deposit amount cannot be zero.");
        }
        switch (accountPlan) {
            case PLATINUM -> {
                if (amount > DEPOSIT_LIMIT_PLATINUM) {
                    throw new IllegalArgumentException("Maximum deposit amount for Platinum amount is "
                            + DEPOSIT_LIMIT_PLATINUM + "$.");
                }
            }

            case GOLD -> {
                if (amount > DEPOSIT_LIMIT_GOLD) {
                    throw new IllegalArgumentException("Maximum deposit amount for Gold amount is "
                            + DEPOSIT_LIMIT_GOLD + "$.");
                }
            }

            case SILVER -> {
                if (amount > DEPOSIT_LIMIT_SILVER) {
                    throw new IllegalArgumentException("Maximum deposit amount for Silver amount is "
                            + DEPOSIT_LIMIT_SILVER + "$.");
                }
            }

            case NORMAL -> {
                if (amount > DEPOSIT_LIMIT_NORMAL) {
                    throw new IllegalArgumentException("Maximum deposit amount for Gold amount is "
                            + DEPOSIT_LIMIT_NORMAL + "$.");
                }
            }
            default -> throw new IllegalArgumentException("Invalid Account Plan.");
        }

    }

    public int getSize() {
        return userAccountMap.size();
    }

    public Account getUserAccount(int userId) {
        if (!userAccountMap.containsKey(userId)) {
            throw new IllegalArgumentException("No account found.");
        }
        return userAccountMap.get(userId);
    }

    public void writeAccountsIntoFile() {
        CsvMapper csvMapper = new CsvMapper();
        CsvSchema schema = CsvSchema.builder()
                .addColumn("accountNumber")
                .addColumn("pin")
                .addColumn("userId")
                .addColumn("type")
                .addColumn("plan")
                .addColumn("balance")
                .addColumn("withdrawLimit")
                .addColumn("depositLimit")
                .addColumn("isActive")
                .addColumn("overdraftCounter")
                .addColumn("favoriteAccounts")
                .setUseHeader(true)
                .build();
        try {
            csvMapper.writer(schema).writeValue(dataSource, userAccountMap.values());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void transfer(int senderAccountNumber, int receiverAccountNumber, double amount) {
        if (!accountNumberMap.containsKey(receiverAccountNumber)) {
            throw new IllegalArgumentException("Invalid receiver account Number");
        }
        if (!accountNumberMap.containsKey(senderAccountNumber)) {
            throw new IllegalArgumentException("Invalid sender account Number");
        }
        if (accountNumberMap.get(receiverAccountNumber).getDepositLimit() >= amount) {
            accountNumberMap.get(senderAccountNumber).withdraw(amount);
            logUserTransaction(accountNumberMap.get(senderAccountNumber), TransactionType.DEBIT, amount);
            accountNumberMap.get(receiverAccountNumber).deposit(amount);
            logUserTransaction(accountNumberMap.get(receiverAccountNumber), TransactionType.CREDIT, amount);
        }
    }

    public void deactivateAccount(Account account) {
        validateAccountNumber(account.getAccountNumber());
        account.setActive(false);
    }

    public void logUserTransaction(Account account, TransactionType transactionType, double amount) {
        transactionRepository.addTransaction(account, transactionType, amount);
    }

    public void validatePin(String pin) {
        if (!pin.matches("^\\d{4}$")) {
            throw new InvalidPinException("Invalid pin format! only 4 digit.");
        }
    }

    public List<Account> getAllFavoriteAccount(Account account) {
        validateAccountNumber(account.getAccountNumber());
        return accountNumberMap.get(account.getAccountNumber()).getFavoriteAccounts().stream()
                .map(id -> accountNumberMap.get(id)).toList();
    }

    public void addNewFavoriteAccount(Account account, Account favoriteAccount) {
        validateAccountNumber(account.getAccountNumber());
        validateAccountNumber(favoriteAccount.getAccountNumber());
        accountNumberMap.get(account.getAccountNumber()).getFavoriteAccounts().add(favoriteAccount.getAccountNumber());
    }

    public void removeFavoriteAccount(Account account, Account favoriteAccount) {
        validateAccountNumber(account.getAccountNumber());
        validateAccountNumber(favoriteAccount.getAccountNumber());
        accountNumberMap.get(account.getAccountNumber()).getFavoriteAccounts().remove(favoriteAccount.getAccountNumber());
    }

    public void validateAccountNumber(int accountNumber) {
        if (!accountNumberMap.containsKey(accountNumber)) {
            throw new IllegalArgumentException("Invalid account number.");
        }
    }

    public Account getAccount(int accountNumber) {
        validateAccountNumber(accountNumber);
        return accountNumberMap.get(accountNumber);

    }
}
