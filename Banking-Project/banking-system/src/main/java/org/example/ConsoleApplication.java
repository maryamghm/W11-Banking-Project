package org.example;

import org.example.domain.*;
import org.example.repository.AccountRepository;
import org.example.repository.TransactionRepository;
import org.example.repository.UserRepository;

import java.io.File;
import java.util.Arrays;
import java.util.Scanner;

public class ConsoleApplication {
    private UserRepository userRepository;
    private AccountRepository accountRepository;
    private final Scanner scanner = new Scanner(System.in);
    private User loggedInUser = null;
    private Account userAccount = null;

    public void run() {
        userRepository = new UserRepository(new File("users.csv"));
        accountRepository = new AccountRepository(new File("accounts.csv"));
        boolean isRunning = true;
        System.out.println("Welcome");
        while (isRunning) {
            if (loggedInUser == null) {
                isRunning = showNoLoginMenu();
            } else {
                switch (loggedInUser.getType()) {
                    case CUSTOMER -> {
                        isRunning = showCustomerMenu();
                    }
                    case EMPLOYEE -> {
                        isRunning = showEmployeeMenu();
                    }
                }
            }
        }
    }

    private boolean showNoLoginMenu() {
        System.out.println("Menu:");
        System.out.println("1. SignUp");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        try {
            int choice = scanner.nextInt();
            scanner.nextLine();
            switch (choice) {
                case 1 -> {
                    showSignupPrompt();
                }
                case 2 -> {
                    showLoginPrompt();
                }
                case 3 -> {
                    System.out.println("Goodbye!");
                    userRepository.writeUsersInFile();
                    accountRepository.writeAccountsIntoFile();
                    return false;
                }
                default -> System.out.println("Choose 1-3: ");
            }
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
        }
        return true;
    }

    private boolean showCustomerMenu() {
        System.out.println("Menu For Customers: ");
        System.out.println("1. Show Balance");
        System.out.println("2. Deposit");
        System.out.println("3. Withdraw");
        System.out.println("4. Transfer");
        System.out.println("5. Show Transactions History");
        System.out.println("6. Reset Password");
        System.out.println("7. Deactivate account");
        System.out.println("8. Logout");
        try {
            int choice = scanner.nextInt();
            scanner.nextLine();
            switch (choice) {
                case 1 -> showShowBalancePrompt();
                case 2 -> showDepositPrompt();
                case 3 -> showWithdrawPrompt();
                case 4 -> showTransferPrompt();
                case 5 -> showTransactionHistoryPrompt();
                case 6 -> ShowResetPasswordPrompt();
                case 7 -> showDeactivateAccountPrompt();
                case 8 -> logOut();
                default -> System.out.println("Choose 1-5: ");
            }
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
        }
        return true;
    }

    private void ShowResetPasswordPrompt() {
        System.out.println("Enter your old password:");
        String oldPassword = scanner.nextLine();
        System.out.println("Enter new password: ");
        String newPassword = scanner.nextLine();
        userRepository.resetPassword(loggedInUser.getUsername(), oldPassword, newPassword);
    }

    private void showTransactionHistoryPrompt() {
        inputAccountPin();
        TransactionRepository transactionRepository = new TransactionRepository(new File("transactions.csv"));
        System.out.println("Your transactions history:");
        System.out.println(transactionRepository.getTransactions(userAccount));
    }

    private void showShowBalancePrompt() {
        inputAccountPin();
        System.out.println("Your current balance: "
                + userAccount.getBalance()
                + "$.");
    }

    private void inputAccountPin() {
        System.out.println("Enter your account's pin: ");
        String pin = scanner.nextLine();
        userAccount.validatePin(pin);
    }

    private void showDeactivateAccountPrompt() {
        inputAccountPin();
        System.out.println("Are you sure you want to deactivate your account? (y/n)");
        try {
            String userInput = scanner.nextLine();
            if (userInput.equalsIgnoreCase("Y")) {
                accountRepository.deactivateAccount(userAccount);
                userRepository.deactivateUser(loggedInUser);
                System.out.println("Your account was successfully deactivate! Goodbye!");
                logOut();
            } else if (userInput.equalsIgnoreCase("N")) {
                System.out.println("Good that you stay with us.");
            } else {
                throw new IllegalArgumentException("Invalid input.");
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void showTransferPrompt() {
        inputAccountPin();
        try {
            System.out.println("Enter the accountNumber you want to transfer to: ");
            int receiverAccountNumber = scanner.nextInt();
            scanner.nextLine();
            System.out.println("How much do you want to transfer? ");
            double amount = scanner.nextDouble();
            scanner.nextLine();
            System.out.println("Do you want to transfer "
                    + amount + "$ to "
                    + accountRepository.getReceiverInfo(receiverAccountNumber, userRepository)
                    + "? (y/n)");
            String userAgreement = scanner.nextLine();
            if (userAgreement.equalsIgnoreCase("Y")) {
                accountRepository.transfer(userAccount.getAccountNumber(), receiverAccountNumber, amount);
            } else if (userAgreement.equalsIgnoreCase("N")) {
                System.out.println("Transfer canceled.");
            } else throw new IllegalArgumentException("Wrong input");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private boolean showEmployeeMenu() {
        return true;
    }

    private void showLoginPrompt() {
        System.out.println("Welcome! please log into system:");
        System.out.println("Please enter your username: ");
        String username = scanner.nextLine();

        System.out.println("Please enter your password: ");
        String password = scanner.nextLine();

        loggedInUser = userRepository.login(username, password);
        if (loggedInUser.getType() == UserType.CUSTOMER) {
            userAccount = accountRepository.getUserAccount(loggedInUser.getId());
        }
    }

    private void showSignupPrompt() {
        System.out.println("Please enter your username: ");
        String username = scanner.nextLine();

        System.out.println("Please enter your password: ");
        String password = scanner.nextLine();

        User user = userRepository.signUp(username, password);


        AccountType type = inputType();
        AccountPlan accountPlan = inputPlan();
        double withdrawLimit = getWithdrawLimit(accountPlan);
        double initialDeposit = inputInitialDeposit(accountPlan);
        String pin = inputPin();

        userAccount = accountRepository.addNewAccount(user.getId(), pin, type, accountPlan, withdrawLimit, initialDeposit);
        showLoginPrompt();
    }

    private String inputPin() {
        String pin = "";
        boolean isInputValid = true;
        do {
            System.out.println("Enter your account pin (4 digits): ");
            try {
                pin = scanner.nextLine();
                if (!pin.matches("^\\d{4}$")) {
                    throw new IllegalArgumentException("Invalid pin format! only 4 digit.");
                }

            } catch (Exception e) {
                isInputValid = false;
                throw new RuntimeException(e);
            }
        } while (!isInputValid);
        return pin;
    }

    private double getWithdrawLimit(AccountPlan plan) {
        double withdrawLimit = 0.0;
        if (plan == AccountPlan.NORMAL) {
            boolean isInputValid = true;
            do {
                System.out.println("Enter your maximum withdraw limit: ");
                try {
                    withdrawLimit = scanner.nextDouble();
                    scanner.nextLine();
                    accountRepository.validateNormalWithdrawLimit(withdrawLimit);
                } catch (Exception e) {
                    isInputValid = false;
                    throw new RuntimeException(e);
                }
            } while (!isInputValid);
        } else {
            withdrawLimit = accountRepository.getWithdrawLimit(plan);
        }
        return withdrawLimit;
    }

    private AccountPlan inputPlan() {
        boolean isInputValid = true;
        int accountPlan = -1;
        do {
            System.out.println("What kind of Plan do you want? "
                    + String.join(", ", Arrays.stream(AccountPlan.values())
                    .map(plan -> plan.ordinal() + ":" + plan.name()).toList()));
            try {
                accountPlan = scanner.nextInt();
                scanner.nextLine();
                if (accountPlan < 0 || accountPlan >= AccountPlan.values().length) {
                    throw new IllegalArgumentException("Invalid plan type: " + accountPlan);
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
                isInputValid = false;
            }
        } while (!isInputValid);
        return AccountPlan.values()[accountPlan];
    }

    private AccountType inputType() {
        boolean isInputValid = true;
        int type = -1;
        do {
            System.out.println("What kind of Account do you want? "
                    + String.join(", ", Arrays.stream(AccountType.values())
                    .map(plan -> plan.ordinal() + ":" + plan.name()).toList()));
            try {
                type = scanner.nextInt();
                scanner.nextLine();
                if (type < 0 || type >= AccountType.values().length) {
                    throw new IllegalArgumentException("Invalid account type: " + type);
                }

            } catch (Exception e) {
                isInputValid = false;
                System.out.println("Invalid account type!");
            }
        } while (!isInputValid);
        return AccountType.values()[type];
    }

    private double inputInitialDeposit(AccountPlan accountPlan) {
        boolean isInputValid = true;
        double initialDeposit = 0.0;
        do {
            System.out.println("How much do you want to deposit to your account? ");
            try {
                initialDeposit = scanner.nextDouble();
                scanner.nextLine();
                accountRepository.validateDepositLimit(initialDeposit, accountPlan);

            } catch (Exception e) {
                isInputValid = false;
                System.out.println("Invalid account type!");
            }
        } while (!isInputValid);
        return initialDeposit;
    }

    private void showDepositPrompt() {
        inputAccountPin();
        System.out.println("How much do you want to deposit to your account? ");
        try {
            double amount = scanner.nextDouble();
            scanner.nextLine();
            if (amount <= 0.0) {
                throw new IllegalArgumentException("Invalid amount.");
            }
            userAccount.deposit(amount);
            accountRepository.logUserTransaction(userAccount, TransactionType.CREDIT, amount);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void showWithdrawPrompt() {
        inputAccountPin();
        System.out.println("How much do you want to withdraw from your account? ");
        try {
            double amount = scanner.nextDouble();
            scanner.nextLine();
            if (amount <= 0.0) {
                throw new IllegalArgumentException("Invalid amount.");
            }
            userAccount.withdraw(amount);
            accountRepository.logUserTransaction(userAccount, TransactionType.DEBIT, amount);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void logOut() {
        userRepository.logout(loggedInUser.getUsername());
        loggedInUser = null;
        userAccount = null;
    }
}
