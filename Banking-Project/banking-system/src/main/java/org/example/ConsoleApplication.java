package org.example;

import org.example.domain.*;
import org.example.exception.InvalidPasswordException;
import org.example.exception.InvalidPinException;
import org.example.exception.InvalidUserNameException;
import org.example.repository.AccountRepository;
import org.example.repository.TransactionRepository;
import org.example.repository.UserRepository;
import org.example.utils.Colors;
import org.example.utils.Logger;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
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
        Logger.printStartEnd("Welcome to Banking Management System!");
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
        Logger.printMainMenu("Menu:");
        Logger.printMainMenu("1. SignUp");
        Logger.printMainMenu("2. Login");
        Logger.printMainMenu("3. Exit");
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
                default -> Logger.warning("Choose an action between 1-3:");
            }
        } catch (Exception e) {
            Logger.error("ERROR: " + e.getMessage());
        }
        return true;
    }

    private void exitSystem() {
        Logger.printStartEnd("Goodbye!");
        userRepository.writeUsersInFile();
        accountRepository.writeAccountsIntoFile();
        transactionRepository.saveTransactions();
    }

    private boolean showCustomerMenu() {
        Logger.printCustomerMenu("Menu For Customers:");
        Logger.printCustomerMenu("1. Show Balance");
        Logger.printCustomerMenu("2. Deposit");
        Logger.printCustomerMenu("3. Withdraw");
        Logger.printCustomerMenu("4. Transfer");
        Logger.printCustomerMenu("5. Favorite Account");
        Logger.printCustomerMenu("6. Show Transactions History");
        Logger.printCustomerMenu("7. Reset Password");
        Logger.printCustomerMenu("8. Deactivate account");
        Logger.printCustomerMenu("9. Logout");
        try {
            int choice = scanner.nextInt();
            scanner.nextLine();
            switch (choice) {
                case 1 -> showShowBalancePrompt();
                case 2 -> showDepositPrompt();
                case 3 -> showWithdrawPrompt();
                case 4 -> showTransferPrompt();
                case 5 -> showFavoriteAccountsMenu();
                case 6 -> showTransactionHistoryPrompt();
                case 7 -> ShowResetPasswordPrompt();
                case 8 -> showDeactivateAccountPrompt();
                case 9 -> logOut();
                default -> Logger.warning("Choose an action between 1-9: ");
            }
        } catch (Exception e) {
            Logger.error("ERROR: " + e.getMessage());
        }
        return true;
    }

    private void showFavoriteAccountsMenu() {
        Logger.printSubMenu("Choose one action: ");
        Logger.printSubMenu("1. Show Favorite Accounts");
        Logger.printSubMenu("2. Add a new Favorite Account");
        Logger.printSubMenu("3. Remove a Favorite Account");
        Logger.printSubMenu("4. Exit Favorite Accounts Menu");
        try {
            int choice = scanner.nextInt();
            scanner.nextLine();
            switch (choice) {
                case 1 -> showAlFavoriteAccounts();
                case 2 -> addNewFavoriteAccount();
                case 3 -> removeFavoriteAccount();
                case 4 -> Logger.printHint("Backing to main menu ..");
                default -> Logger.warning("Choose an action between 1-4: ");
            }
        } catch (Exception e) {
            Logger.error("ERROR: " + e.getMessage());
        }
    }

    private void removeFavoriteAccount() {
        inputAccountPin();
        showAlFavoriteAccounts();
        System.out.println("Please enter an account number to be removed from your favorite account list:");
        try {
            int accountNumber = scanner.nextInt();
            scanner.nextLine();
            accountRepository.validateAccountNumber(accountNumber);
            Account favoriteAccount = accountRepository.getAccount(accountNumber);
            String userFullName = userRepository.getUserInfo(favoriteAccount.getUserId());
            System.out.println("Are you sure you want to remove " + userFullName + " from your favorites? (y/n)");
            String userInput = scanner.nextLine();
            if (userInput.equalsIgnoreCase("N")) {
                System.out.println("Removing favorite account aborted.");
            } else if (userInput.equalsIgnoreCase("Y")) {
                accountRepository.removeFavoriteAccount(userAccount, favoriteAccount);
            } else {
                throw new IllegalArgumentException("Invalid user's input.");
            }
        } catch (Exception e) {
            Logger.error("ERROR: " + e.getMessage());
        }

    }

    private void addNewFavoriteAccount() {
        inputAccountPin();
        System.out.println("Please enter a valid account number to be added to your favorite account list:");
        try {
            int accountNumber = scanner.nextInt();
            scanner.nextLine();
            accountRepository.validateAccountNumber(accountNumber);
            Account favoriteAccount = accountRepository.getAccount(accountNumber);
            String userFullName = userRepository.getUserInfo(favoriteAccount.getUserId());
            System.out.println("Are you sure you want to add " + userFullName + " as a favorite account? (y/n)");
            String userInput = scanner.nextLine();
            if (userInput.equalsIgnoreCase("N")) {
                System.out.println("Adding favorite account aborted.");
            } else if (userInput.equalsIgnoreCase("Y")) {
                accountRepository.addNewFavoriteAccount(userAccount, favoriteAccount);
            } else {
                throw new IllegalArgumentException("Invalid user's input.");
            }
        } catch (Exception e) {
            Logger.error("ERROR: " + e.getMessage());
        }
    }

    private void showAlFavoriteAccounts() {
        inputAccountPin();
        List<Integer> favoriteAccounts = accountRepository.getAllFavoriteAccount(userAccount);
        if (favoriteAccounts.isEmpty()) {
            System.out.println("You have no favorite account list yet.");
            return;
        }
        System.out.println("Your favorite accounts IDs: ");
        System.out.println(favoriteAccounts);
    }

    private void ShowResetPasswordPrompt() {
        inputAccountPin();
        System.out.println("Enter your old password:");
        String oldPassword = scanner.nextLine();
        System.out.println("Enter new password: ");
        String newPassword = scanner.nextLine();
        userRepository.resetPassword(loggedInUser.getUsername(), oldPassword, newPassword);
    }

    private void showTransactionHistoryPrompt() {
        inputAccountPin();
        Logger.printSubMenu("Choose an action:");
        Logger.printSubMenu("1. Show All Transactions");
        Logger.printSubMenu("2. Show Transaction History within a specified date range");
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
            Logger.error("ERROR: " + e.getMessage());
        }
    }

    private void showTransferPrompt() {
        try {
            showAlFavoriteAccounts();
            System.out.println("You can choose an account number from your favorite list or enter a new account number.");
            System.out.println("Enter the accountNumber you want to transfer to: ");
            int receiverAccountNumber = scanner.nextInt();
            scanner.nextLine();
            System.out.println("How much do you want to transfer? ");
            double amount = scanner.nextDouble();
            scanner.nextLine();
            Account receiverAccount = accountRepository.getAccount(receiverAccountNumber);
            if (userAccount.getFavoriteAccounts().contains(receiverAccount.getAccountNumber())) {
                accountRepository.transfer(userAccount.getAccountNumber(), receiverAccountNumber, amount);
                System.out.println("Transfer succeed.");
            } else {
                String userFullName = userRepository.getUserInfo(receiverAccount.getUserId());
                System.out.println("Do you want to transfer "
                        + amount + "$ to "
                        + userFullName
                        + "? (y/n)");
                String userAgreement = scanner.nextLine();
                if (userAgreement.equalsIgnoreCase("Y")) {
                    accountRepository.transfer(userAccount.getAccountNumber(), receiverAccountNumber, amount);
                    System.out.println("Transfer succeed.");
                } else if (userAgreement.equalsIgnoreCase("N")) {
                    System.out.println("Transfer canceled.");
                } else throw new IllegalArgumentException("Invalid user input.");
            }

        } catch (Exception e) {
            Logger.error("ERROR: " + e.getMessage());
        }
    }

    private boolean showEmployeeMenu() {
        return true;
    }

    private void showLoginPrompt() {
        System.out.println("Welcome! please log into system:");
        String username = inputUsername(false);
        if (username.equals("-1")) {
            return;
        }
        System.out.println(Colors.colorize("To go back to the main menu please enter -1", Colors.ANSI_BOLD_YELLOW));
        System.out.println("Please enter your password: ");
        String password = scanner.nextLine();
        if (Objects.equals(password, "-1")) {
            return;
        }
        loggedInUser = userRepository.login(username, password);
        if (loggedInUser.getType() == UserType.CUSTOMER) {
            userAccount = accountRepository.getUserAccount(loggedInUser.getId());
        }
    }

    private void showSignupPrompt() {
        //get username
        String username = inputUsername(true);
        if (username.equals("-1")) {
            return;
        }

        //get password
        String password = inputPassword();
        if (password.equals("-1")) {
            return;
        }

        //get firstname
        String firstName = inputFirstName();
        if (firstName.equals("-1")) {
            return;
        }

        //get lastname
        String lastName = inputLastName();
        if (lastName.equals("-1")) {
            return;
        }

        //get account type
        int inputType = inputType();
        if (inputType == -1) {
            return;
        }
        AccountType type = AccountType.values()[inputType];

        //get account plan
        int inputPlan = inputPlan();
        if (inputPlan == -1) {
            return;
        }
        AccountPlan accountPlan = AccountPlan.values()[inputPlan];

        //get withdraw limit
        double withdrawLimit = getWithdrawLimit(accountPlan);
        if (withdrawLimit == -1) {
            return;
        }
        //get initial deposit amount
        double initialDeposit = inputInitialDeposit(accountPlan);
        if (initialDeposit == -1) {
            return;
        }
        String pin = inputPin();

        if (pin.equals("-1")) {
            return;
        }

        User user = userRepository.signUp(username, password, firstName, lastName);

        userAccount = accountRepository.addNewAccount(user.getId(), pin, type, accountPlan, withdrawLimit, initialDeposit);
        showLoginPrompt();
    }

    private String inputFirstName() {
        String firstName;
        do {
            try {
                System.out.println(Colors.colorize("To go back to the main menu please enter -1", Colors.ANSI_BOLD_YELLOW));
                System.out.println("Please enter your firstname: ");
                firstName = scanner.nextLine();
                if (Objects.equals(firstName, "-1")) {
                    break;
                }
                userRepository.validateFirstName(firstName);
                break;
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } while (true);
        return firstName;
    }

    private String inputLastName() {
        String lastName;
        do {
            try {
                System.out.println(Colors.colorize("To go back to the main menu please enter -1", Colors.ANSI_BOLD_YELLOW));
                System.out.println("Please enter your lastname: ");
                lastName = scanner.nextLine();
                if (Objects.equals(lastName, "-1")) {
                    break;
                }
                userRepository.validateLastName(lastName);
                break;
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } while (true);
        return lastName;
    }

    private String inputPassword() {
        String password;
        do {
            try {
                System.out.println(Colors.colorize("To go back to the main menu please enter -1", Colors.ANSI_BOLD_YELLOW));
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
                System.out.println(Colors.colorize("To go back to the main menu please enter -1", Colors.ANSI_BOLD_YELLOW));
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
                System.out.println(Colors.colorize("To go back to the main menu please enter -1", Colors.ANSI_BOLD_YELLOW));
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
                    System.out.println(Colors.colorize("To go back to the main menu please enter -1", Colors.ANSI_BOLD_YELLOW));
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
                System.out.println(Colors.colorize("To go back to the main menu please enter -1", Colors.ANSI_BOLD_YELLOW));
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
                System.out.println(Colors.colorize("To go back to the main menu please enter -1", Colors.ANSI_BOLD_YELLOW));
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
                System.out.println(Colors.colorize("To go back to the main menu please enter -1", Colors.ANSI_BOLD_YELLOW));
                System.out.println(Colors.colorize("Your deposit limit: " + accountRepository.getDepositLimit(accountPlan), Colors.ANSI_CYAN));
                System.out.println("How much do you want to deposit to your account?");

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
        System.out.println(Colors.colorize("Your deposit limit: " + accountRepository.getDepositLimit(userAccount.getPlan()), Colors.ANSI_CYAN));
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
        System.out.println(Colors.colorize("Your withdraw limit: " + accountRepository.getWithdrawLimit(userAccount.getPlan()), Colors.ANSI_CYAN));
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
