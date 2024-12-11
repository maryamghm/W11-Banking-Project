package org.example.repository;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.Data;
import org.example.domain.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static TransactionRepository transactionRepository = new TransactionRepository(new File("transactions.csv"));

    private int accountNumberCounter;
    private final File dataSource;

    public AccountRepository(File dataSource) {

        this.dataSource = dataSource;
        populateAccounts();
    }

    private void populateAccounts() {
        CsvMapper csvMapper = new CsvMapper();
        CsvSchema schema = csvMapper.schemaFor(Account.class)
                .withHeader()
                .withColumnReordering(true);

        ObjectReader reader = csvMapper.readerFor(Account.class).with(schema);

        try {
            List<Object> objects = reader.readValues(dataSource).readAll();
            for (Object obj : objects) {
                if (obj instanceof Account) {
                    userAccountMap.put(((Account) obj).getUserId(), (Account) obj);
                    accountNumberMap.put(((Account) obj).getAccountNumber(), (Account) obj);
                }
            }
            userAccountMap.values().stream()
                    .map(Account::getAccountNumber)
                    .max(Integer::compareTo)
                    .orElse(0);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public Account addNewAccount(Integer userId, String pin, AccountType type, AccountPlan plan, double withdrawLimit, double initialDeposit) {
        Account newAccount = null;
        switch (type) {
            case AccountType.CHECKING_ACCOUNT -> {
                newAccount = new CheckingAccount();
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
        logUserTransaction(newAccount, TransactionType.CREDIT, newAccount.getBalance());

        userAccountMap.put(userId, newAccount);
        accountNumberMap.put(newAccount.getAccountNumber(), newAccount);
        return newAccount;
    }


    public void validateNormalWithdrawLimit(double withdrawLimit) {
        if (withdrawLimit >= WITHDRAW_LIMIT_NORMAL) {
            throw new IllegalArgumentException("Withdraw limit for a normal user can be maximum " + WITHDRAW_LIMIT_NORMAL + "$.");
        }
    }

    public void validateWithdrawLimit(double withdrawLimit) {
        if (withdrawLimit >= WITHDRAW_LIMIT_NORMAL) {
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
                .setUseHeader(true)
                .build();
        try {
            csvMapper.writer(schema).writeValue(dataSource, userAccountMap.values());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getReceiverInfo(int receiverAccountNumber, UserRepository userRepository) {
        if (!accountNumberMap.containsKey(receiverAccountNumber)) {
            throw new IllegalArgumentException("Invalid receiver account Number");
        }
        int userId = accountNumberMap.get(receiverAccountNumber).getUserId();
        return userRepository.getUserInfo(userId);
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
        if (!accountNumberMap.containsKey(account.getAccountNumber())) {
            throw new IllegalArgumentException("Invalid account number.");
        }
        account.setActive(false);
    }

    public void logUserTransaction(Account account, TransactionType transactionType, double amount) {
        transactionRepository.addTransaction(account, transactionType, amount);
    }
}
