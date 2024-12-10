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
    private Map<Integer, Account> userAccountList = new HashMap<>();

    private int accountNumberCounter;
    private final File dataSource;

    public AccountRepository(File dataSource) {

        this.dataSource = dataSource;
        populateAccounts();
    }

    public Account addNewAccount(Integer userId, AccountType type, AccountPlan plan, double withdrawLimit, double initialDeposit) {
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
        newAccount.setAccountNumber(++accountNumberCounter);
        newAccount.setPlan(plan);
        newAccount.setType(type);
        newAccount.setWithdrawLimit(withdrawLimit);
        newAccount.setBalance(initialDeposit);
        newAccount.setDepositLimit(getDepositLimit(plan));

        userAccountList.put(userId, newAccount);
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
        }
        throw new IllegalArgumentException("Invalid Account Plan.");
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
                    userAccountList.put(((Account) obj).getUserId(), (Account) obj);
                }
            }
            userAccountList.values().stream()
                    .map(Account::getAccountNumber)
                    .max(Integer::compareTo)
                    .orElse(0);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public double showUserAccountBalance(int userId) {
        if (!userAccountList.containsKey(userId)) {
            throw new IllegalArgumentException("No account found.");
        }
        return userAccountList.get(userId).getBalance();
    }

    public void deposit(int userId, double amount) {
        if (!userAccountList.containsKey(userId)) {
            throw new IllegalArgumentException("No account found.");
        }
        userAccountList.get(userId).deposit(amount);
    }

    public void withdraw(int userId, double amount) {
        if (!userAccountList.containsKey(userId)) {
            throw new IllegalArgumentException("No account found.");
        }
        userAccountList.get(userId).withdraw(amount);
    }

    public int getSize() {
        return userAccountList.size();
    }
}
