package org.example;

import org.example.domain.*;
import org.example.exception.InvalidPasswordException;
import org.example.exception.InvalidPinException;
import org.example.exception.InvalidUserNameException;
import org.example.repository.AccountRepository;
import org.example.repository.TransactionRepository;
import org.example.repository.UserRepository;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class ConsoleApplication {
    private UserRepository userRepository;
    private AccountRepository accountRepository;
    private TransactionRepository transactionRepository;
    private final Scanner scanner = new Scanner(System.in);
    private User loggedInUser = null;
    private Account userAccount = null;

    public void run() {
        userRepository = UserRepository.getInstance(new File("users.csv"));
        accountRepository = AccountRepository.getInstance(new File("accounts.csv"));
        transactionRepository = TransactionRepository.getInstance(new File("transactions.csv"));
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
                    exitSystem();
                    return false;
                }
                default -> System.out.println("Choose 1-3: ");
            }
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
        }
        return true;
    }

    private void exitSystem() {
        System.out.println("Goodbye!");
        userRepository.writeUsersInFile();
        accountRepository.writeAccountsIntoFile();
        transactionRepository.saveTransactions();
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
        System.out.println("Choose:");
        System.out.println("1. Show All Transactions");
        System.out.println("2. Show Transaction History within a specified date range");
        try {
            int choice = scanner.nextInt();
            scanner.nextLine();
            switch (choice) {
                case 1 -> {
                    System.out.println("Your transactions history:");
                    System.out.println(transactionRepository.getTransactions(userAccount));
                }
                case 2 -> {
                    getDateAndShowTransactions();
                }
                default -> {
                    throw new IllegalArgumentException("Wrong input. Accepted values: 1 or 2.");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Wrong input. Accepted values: 1 or 2.");
        }
    }

    private void getDateAndShowTransactions() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        System.out.println("Enter a start date in this format (dd-MM-yyyy): ");
        String startDateString = scanner.nextLine();
        LocalDate fromDate = LocalDate.parse(startDateString, formatter);
        if (fromDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("The date cannot be in the future. Please try again.");
        }
        System.out.println("Enter a end date in this format (dd-MM-yyyy): ");
        String toDateString = scanner.nextLine();
        LocalDate toDate = LocalDate.parse(toDateString, formatter);
        if (toDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("The date cannot be in the future. Please try again.");
        }
        if (toDate.isBefore(fromDate)) {
            throw new IllegalArgumentException("The end date cannot be after start day. Please try again.");
        }

        System.out.println("Your transaction history from " + fromDate + " to " + toDate + ":");
        System.out.println(transactionRepository.getTransactions(userAccount, fromDate, toDate));

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
        String username = inputUsername(false);
        System.out.println("Enter yor password:");
        String password = scanner.nextLine();

        loggedInUser = userRepository.login(username, password);
        if (loggedInUser.getType() == UserType.CUSTOMER) {
            userAccount = accountRepository.getUserAccount(loggedInUser.getId());
        }
    }

    private void showSignupPrompt() {
        String username = null;
        String password = null;
        username = inputUsername(true);
        if (username.equals("-1")) {
            return;
        }
        password = inputPassword();
        if (password.equals("-1")) {
            return;
        }


        int inputType = inputType();
        if (inputType == -1) {
            return;
        }
        AccountType type = AccountType.values()[inputType];

        int inputPlan = inputPlan();
        if (inputPlan == -1) {
            return;
        }
        AccountPlan accountPlan = AccountPlan.values()[inputPlan];
        double withdrawLimit = getWithdrawLimit(accountPlan);
        if (withdrawLimit == -1) {
            return;
        }
        double initialDeposit = inputInitialDeposit(accountPlan);

        if (initialDeposit == -1) {
            return;
        }
        String pin = inputPin();

        if (pin.equals("-1")) {
            return;
        }

        User user = userRepository.signUp(username, password);

        userAccount = accountRepository.addNewAccount(user.getId(), pin, type, accountPlan, withdrawLimit, initialDeposit);
        showLoginPrompt();
    }

    private String inputPassword() {
        String password;
        do {
            try {
                System.out.println("To go back to the main menu please enter -1");
                System.out.println("Please enter your password: ");
                password = scanner.nextLine();
                if (Objects.equals(password, "-1")) {
                    break;
                }
                userRepository.validatePassword(password);
                break;
            } catch (InvalidPasswordException e) {
                System.out.println(e.getMessage());
            }
        } while (true);
        return password;
    }

    private String inputUsername(boolean isNewUser) {
        String username;
        do {
            try {
                System.out.println("To go back to the main menu please enter -1");
                System.out.println("Please enter your username: ");
                username = scanner.nextLine();
                if (Objects.equals(username, "-1")) {
                    break;
                }
                userRepository.validateUsername(username, isNewUser);
                break;
            } catch (InvalidUserNameException e) {
                System.out.println(e.getMessage());
            }
        } while (true);
        return username;
    }

    private String inputPin() {
        String pin;
        do {
            try {
                System.out.println("To go back to the main menu please enter -1");
                System.out.println("Set your account pin (4 digits): ");
                pin = scanner.nextLine();
                if (Objects.equals(pin, "-1")) {
                    break;
                }
                accountRepository.validatePin(pin);
                ;
                break;
            } catch (InvalidPinException e) {
                System.out.println(e.getMessage());
            }
        } while (true);
        return pin;
    }

    private double getWithdrawLimit(AccountPlan plan) {
        double withdrawLimit = 0.0;
        if (plan == AccountPlan.NORMAL) {
            do {
                try {
                    System.out.println("To go back to the main menu please enter -1");
                    System.out.println("Enter your maximum withdraw limit: ");
                    withdrawLimit = scanner.nextDouble();
                    scanner.nextLine();
                    if (withdrawLimit == -1) {
                        break;
                    }
                    accountRepository.validateNormalWithdrawLimit(withdrawLimit);
                    break;
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            } while (true);
        } else {
            withdrawLimit = accountRepository.getWithdrawLimit(plan);
        }
        return withdrawLimit;
    }

    private int inputPlan() {
        int accountPlan;
        do {
            try {
                System.out.println("To go back to the main menu please enter -1");
                System.out.println("What kind of Plan do you want? "
                    + String.join(", ", Arrays.stream(AccountPlan.values())
                    .map(plan -> plan.ordinal() + ":" + plan.name()).toList()));

                accountPlan = scanner.nextInt();
                scanner.nextLine();
                if (accountPlan == -1) {
                    break;
                }
                if (accountPlan < 0 || accountPlan >= AccountPlan.values().length) {
                    throw new IllegalArgumentException("Invalid plan type: " + accountPlan);
                }
                break;
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } while (true);
        return accountPlan;
    }

    private int inputType() {
        int type;
        do {
            try {
                System.out.println("To go back to the main menu please enter -1");
                System.out.println("What kind of Account do you want? "
                    + String.join(", ", Arrays.stream(AccountType.values())
                    .map(plan -> plan.ordinal() + ":" + plan.name()).toList()));
                type = scanner.nextInt();
                scanner.nextLine();
                if (type == -1) {
                    break;
                }
                if (type < 0 || type >= AccountType.values().length) {
                    throw new IllegalArgumentException("Invalid account type: " + type);
                }
                break;

            } catch (Exception e) {
                System.out.println("Invalid account type!");
            }
        } while (true);
        return type;
    }

    private double inputInitialDeposit(AccountPlan accountPlan) {
        double initialDeposit;
        do {
            try {
                System.out.println("To go back to the main menu please enter -1");
                System.out.println("How much do you want to deposit to your account? ");
                initialDeposit = scanner.nextDouble();
                scanner.nextLine();

                if (initialDeposit == -1) {
                    break;
                }
                accountRepository.validateDepositLimit(initialDeposit, accountPlan);
                break;

            } catch (Exception e) {
                System.out.println("Invalid account type!");
            }
        } while (true);
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
