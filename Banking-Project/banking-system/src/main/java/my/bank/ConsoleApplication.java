package my.bank;

import my.bank.domain.*;
import my.bank.exception.InvalidPasswordException;
import my.bank.exception.InvalidPinException;
import my.bank.exception.InvalidUserNameException;
import my.bank.repository.AccountRepository;
import my.bank.repository.TransactionRepository;
import my.bank.repository.UserRepository;
import my.bank.utils.Logger;

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
        Logger.printStartEnd("Welcome to Banking Management System\n");
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
        Logger.printMainMenu("1. Sign Up for a New Account");
        Logger.printMainMenu("2. Log In to Your Account");
        Logger.printMainMenu("3. Exit the System");
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
                default -> Logger.warning("Please select an option (1-3):");
            }
        } catch (Exception e) {
            Logger.error("An unexpected error occurred: " + e.getMessage() + ". Please try again.");
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
        Logger.printStartEnd("Menu For Customers:\n");
        Logger.printMainMenu("1. Show Balance");
        Logger.printMainMenu("2. Deposit");
        Logger.printMainMenu("3. Withdraw");
        Logger.printMainMenu("4. Transfer");
        Logger.printMainMenu("5. Favorite Account");
        Logger.printMainMenu("6. Show Transactions History");
        Logger.printMainMenu("7. Reset Password");
        Logger.printMainMenu("8. Deactivate account");
        Logger.printMainMenu("9. Logout");
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
                default -> Logger.warning("Please select an option (1-9):");
            }
        } catch (Exception e) {
            Logger.error("An unexpected error occurred: " + e.getMessage() + ". Please try again.");
        }
        return true;
    }

    private void showFavoriteAccountsMenu() {
        Logger.printStartEnd("Choose one action:");
        Logger.printSubMenu("1. Show Favorite Accounts");
        Logger.printSubMenu("2. Add a new Favorite Account");
        Logger.printSubMenu("3. Remove a Favorite Account");
        Logger.printSubMenu("4. Exit Favorite Accounts Menu");
        try {
            int choice = scanner.nextInt();
            scanner.nextLine();
            switch (choice) {
                case 1 -> showAllFavoriteAccounts();
                case 2 -> addNewFavoriteAccount();
                case 3 -> removeFavoriteAccount();
                case 4 -> Logger.printHint("Backing to main menu ..");
                default -> Logger.warning("Choose an action between 1-4: ");
            }
        } catch (Exception e) {
            Logger.error("An unexpected error occurred: " + e.getMessage() + ". Please try again.");
        }
    }

    private void removeFavoriteAccount() {
        showAllFavoriteAccounts();
        Logger.warning("To go back to the main menu please enter -1");
        System.out.println("Please enter an account number to be removed from your favorite account list:");
        try {
            int accountNumber = scanner.nextInt();
            scanner.nextLine();
            if (accountNumber == -1) {
                return;
            }
            accountRepository.validateAccountNumber(accountNumber);
            Account favoriteAccount = accountRepository.getAccount(accountNumber);
            String userFullName = userRepository.getUserInfo(favoriteAccount.getUserId());
            System.out.println("Are you sure you want to remove " + Logger.coloredData(userFullName) + " from your favorites? (y/n)");
            String userInput = scanner.nextLine();
            if (userInput.equalsIgnoreCase("N")) {
                Logger.printInfo("Removing account from your favorite list was canceled.");
            } else if (userInput.equalsIgnoreCase("Y")) {
                accountRepository.removeFavoriteAccount(userAccount, favoriteAccount);
                Logger.printInfo("Account successfully removed from your favorite list. Your changes have been saved.");
            } else {
                throw new IllegalArgumentException("Invalid user's input.");
            }
        } catch (Exception e) {
            Logger.error("An unexpected error occurred: " + e.getMessage() + ". Please try again.");
        }

    }

    private void addNewFavoriteAccount() {
        inputAccountPin();
        Logger.warning("To go back to the main menu please enter -1");
        System.out.println("Please enter a valid account number to be added to your favorite account list:");
        try {
            int accountNumber = scanner.nextInt();
            scanner.nextLine();
            if (accountNumber == -1) {
                return;
            }
            accountRepository.validateAccountNumber(accountNumber);
            Account favoriteAccount = accountRepository.getAccount(accountNumber);
            String userFullName = userRepository.getUserInfo(favoriteAccount.getUserId());
            System.out.println("Are you sure you want to add " + Logger.coloredData(userFullName) + Logger.resetColoredMessage(" as a favorite account? (y/n)"));
            String userInput = scanner.nextLine();
            if (userInput.equalsIgnoreCase("N")) {
                Logger.printInfo("Adding account to your favorite list was canceled.");
            } else if (userInput.equalsIgnoreCase("Y")) {
                accountRepository.addNewFavoriteAccount(userAccount, favoriteAccount);
                Logger.printInfo("Account successfully added to your favorite list. Thank you for using our banking system.");
            } else {
                throw new IllegalArgumentException("Invalid user's input.");
            }
        } catch (Exception e) {
            Logger.error("An unexpected error occurred: " + e.getMessage() + ". Please try again.");
        }
    }

    private void showAllFavoriteAccounts() {
        inputAccountPin();
        List<Account> favoriteAccounts = accountRepository.getAllFavoriteAccount(userAccount);
        if (favoriteAccounts.isEmpty()) {
            Logger.printInfo("You have no favorite account list yet.");
            return;
        }
        List<String> accountInfos = favoriteAccounts.stream()
                .map(account -> account.getAccountNumber() + ": " + userRepository.getUserInfo(account.getUserId()))
                .toList();
        Logger.printInfo("Your favorite accounts: ");
        Logger.printInfo(String.join("\n", accountInfos));
    }

    private void ShowResetPasswordPrompt() {
        inputAccountPin();
        Logger.warning("To go back to the main menu please enter -1");
        System.out.println("Enter your old password:");
        String oldPassword = scanner.nextLine();
        if (oldPassword.equals("-1")) {
            return;
        }
        Logger.warning("To go back to the main menu please enter -1");
        Logger.printInfo("Your password must meet the following criteria:\n"
                + "1. Be at least 6 characters long.\n"
                + "2. Contain at least one lowercase letter.\n"
                + "3. Contain at least one uppercase letter.\n"
                + "4. Include at least one digit.");
        System.out.println("Enter new password: ");
        String newPassword = scanner.nextLine();
        if (newPassword.equals("-1")) {
            return;
        }
        userRepository.resetPassword(loggedInUser.getUsername(), oldPassword, newPassword);
        Logger.printInfo("You have successfully set a new password.");
    }

    private void showTransactionHistoryPrompt() {
        inputAccountPin();
        Logger.printSubMenu("Choose an action:");
        Logger.printSubMenu("1. Show All Transactions");
        Logger.printSubMenu("2. Show Transaction History within a specified date range");
        Logger.printSubMenu("3. Exit");

        try {
            int choice = scanner.nextInt();
            scanner.nextLine();
            switch (choice) {
                case 1 -> {
                    Logger.printInfo("Your transactions history:");
                    Logger.printInfo(String.join("\n", transactionRepository.getTransactions(userAccount).stream().map(Transaction::toString).toList()));
                }
                case 2 -> getDateAndShowTransactions();

                case 3 -> {
                    return;
                }

                default -> Logger.warning("Please select an option (1-2)");

            }
        } catch (Exception e) {
            throw new RuntimeException("Wrong input. Accepted values: 1 or 2.");
        }
    }

    private void getDateAndShowTransactions() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        Logger.warning("To go back to the main menu please enter -1");
        System.out.println("Please enter the start date " + Logger.coloredData("(format: dd-MM-yyyy)") + Logger.resetColoredMessage(": "));
        String startDateString = scanner.nextLine();
        if (startDateString.equals("-1")) {
            return;
        }
        LocalDate fromDate = LocalDate.parse(startDateString, formatter);
        if (fromDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("The date cannot be in the future. Please try again.");
        }
        Logger.warning("To go back to the main menu please enter -1");
        System.out.println("Please enter the end date " + Logger.coloredData("(format: dd-MM-yyyy)") + Logger.resetColoredMessage(": "));
        String toDateString = scanner.nextLine();
        if (toDateString.equals("-1")) {
            return;
        }
        LocalDate toDate = LocalDate.parse(toDateString, formatter);
        if (toDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("The date cannot be in the future. Please try again.");
        }
        if (toDate.isBefore(fromDate)) {
            throw new IllegalArgumentException("The end date must be after the start date. Please try again.");
        }

        Logger.printInfo("Transaction history between " + fromDate + " and " + toDate);
        Logger.printInfo(
                String.join("\n",
                        transactionRepository.getTransactions(userAccount, fromDate, toDate).stream().map(Transaction::toString).toList()
                )
        );

    }

    private void showShowBalancePrompt() {
        inputAccountPin();
        Logger.printInfo("Your current balance: "
                + userAccount.getBalance()
                + "USD.");
    }

    private void inputAccountPin() {
        Logger.printHint("Enter your account's pin: ");
        String pin = scanner.nextLine();
        userAccount.validatePin(pin);
    }

    private void showDeactivateAccountPrompt() {
        inputAccountPin();
        Logger.warning("To go back to the main menu please enter -1");
        System.out.println("Are you sure you want to deactivate your account? This action cannot be undone. (y/n)");
        try {
            String userInput = scanner.nextLine();
            if (userInput.equals("-1")) {
                return;
            }
            if (userInput.equalsIgnoreCase("Y")) {
                accountRepository.deactivateAccount(userAccount);
                userRepository.deactivateUser(loggedInUser);
                Logger.printInfo("Your account was successfully deactivated! Goodbye!");
                logOut();
            } else if (userInput.equalsIgnoreCase("N")) {
                Logger.printInfo("We're glad you decided to stay with us. If you need assistance, let us know!");
            } else {
                throw new IllegalArgumentException("Invalid input.");
            }

        } catch (Exception e) {
            Logger.error("An unexpected error occurred: " + e.getMessage() + ". Please try again.");
        }
    }

    private void showTransferPrompt() {
        try {
            showAllFavoriteAccounts();
            Logger.warning("To go back to the main menu please enter -1");
            System.out.println("You may select an account from your favorites or provide a new account number.");
            System.out.println("Enter the recipient's account number: ");
            int receiverAccountNumber = scanner.nextInt();
            scanner.nextLine();

            if (receiverAccountNumber == -1) {
                return;
            }

            Logger.warning("To go back to the main menu please enter -1");
            System.out.println("How much do you want to transfer? ");
            double amount = scanner.nextDouble();
            scanner.nextLine();
            if (amount == -1) {
                return;
            }
            Account receiverAccount = accountRepository.getAccount(receiverAccountNumber);
            if (userAccount.getFavoriteAccounts().contains(receiverAccount.getAccountNumber())) {
                accountRepository.transfer(userAccount.getAccountNumber(), receiverAccountNumber, amount);
                Logger.printInfo("Transfer completed successfully. Thank you for banking with us.");
            } else {
                String userFullName = userRepository.getUserInfo(receiverAccount.getUserId());
                System.out.println("Do you want to transfer "
                        + amount + "$ to "
                        + Logger.coloredData(userFullName)
                        + Logger.resetColoredMessage("? (y/n)"));
                String userAgreement = scanner.nextLine();
                if (userAgreement.equalsIgnoreCase("Y")) {
                    accountRepository.transfer(userAccount.getAccountNumber(), receiverAccountNumber, amount);
                    Logger.printInfo("Transfer completed successfully. Thank you for banking with us.");
                } else if (userAgreement.equalsIgnoreCase("N")) {
                    Logger.printInfo("Transfer canceled.");
                } else throw new IllegalArgumentException("Invalid user input.");
            }

        } catch (Exception e) {
            Logger.error("An unexpected error occurred: " + e.getMessage() + ". Please try again.");
        }
    }

    private boolean showEmployeeMenu() {
        return true;
    }

    private void showLoginPrompt() {
        Logger.printStartEnd("Please log into system:");
        String username = inputUsername(false);
        if (username.equals("-1")) {
            return;
        }
        String password = inputPassword(false);
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
        String password = inputPassword(true);
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

        Logger.printInfo("You are creating a " + type + " account with the " + accountPlan + " plan. To proceed, please enter 1:");
        int userInput = scanner.nextInt();
        scanner.nextLine();
        if (userInput != 1) {
            return;
        }

        User user = userRepository.signUp(username, password, firstName, lastName);

        userAccount = accountRepository.addNewAccount(user.getId(), pin, type, accountPlan, withdrawLimit, initialDeposit);
        Logger.printInfo("You have successfully created your account.");
        showLoginPrompt();
    }

    private String inputFirstName() {
        String firstName;
        do {
            try {
                Logger.warning("To go back to the main menu please enter -1");
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
                Logger.warning("To go back to the main menu please enter -1");
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

    private String inputPassword(boolean isNewUser) {
        String password;
        do {
            try {
                Logger.warning("To go back to the main menu please enter -1");
                if (isNewUser) {
                    Logger.printInfo("Your password must meet the following criteria:\n"
                            + "1. Be at least 6 characters long.\n"
                            + "2. Contain at least one lowercase letter.\n"
                            + "3. Contain at least one uppercase letter.\n"
                            + "4. Include at least one digit.");
                    System.out.println("Please set your password: ");
                } else {
                    System.out.println("Please enter your password: ");
                }

                password = scanner.nextLine();
                if (Objects.equals(password, "-1")) {
                    break;
                }
                if (isNewUser) {
                    userRepository.validatePassword(password);
                }
                break;
            } catch (InvalidPasswordException e) {
                Logger.error(e.getMessage());
            }
        } while (true);
        return password;
    }

    private String inputUsername(boolean isNewUser) {
        String username;
        do {
            try {
                Logger.warning("To go back to the main menu please enter -1");
                if (isNewUser) {
                    System.out.println("Create a unique username (lowercase, no spaces): ");
                } else {
                    System.out.println("Please enter your username:");
                }
                username = scanner.nextLine();
                if (Objects.equals(username, "-1")) {
                    break;
                }
                if (isNewUser) {
                    userRepository.validateUsername(username, isNewUser);
                }
                break;
            } catch (InvalidUserNameException e) {
                Logger.error(e.getMessage());
            }
        } while (true);
        return username;
    }

    private String inputPin() {
        String pin;
        do {
            try {
                Logger.warning("To go back to the main menu please enter -1");
                System.out.println("Set a secure 4-digit PIN for your account: ");
                pin = scanner.nextLine();
                if (Objects.equals(pin, "-1")) {
                    break;
                }
                accountRepository.validatePin(pin);
                ;
                break;
            } catch (InvalidPinException e) {
                Logger.error(e.getMessage());
            }
        } while (true);
        return pin;
    }

    private double getWithdrawLimit(AccountPlan plan) {
        double withdrawLimit = 0.0;
        if (plan == AccountPlan.NORMAL) {
            do {
                try {
                    Logger.warning("To go back to the main menu please enter -1");
                    System.out.println("Please enter your desired maximum withdraw limit: (up to 200.00 USD)");
                    withdrawLimit = scanner.nextDouble();
                    scanner.nextLine();
                    if (withdrawLimit == -1) {
                        break;
                    }
                    accountRepository.validateNormalWithdrawLimit(withdrawLimit);
                    break;
                } catch (Exception e) {
                    Logger.error(e.getMessage());
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
                Logger.warning("To go back to the main menu please enter -1");
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
                Logger.error(e.getMessage());
            }
        } while (true);
        return accountPlan;
    }

    private int inputType() {
        int type;
        do {
            try {
                Logger.warning("To go back to the main menu please enter -1");
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
                Logger.error("Invalid account type.");
            }
        } while (true);
        return type;
    }

    private double inputInitialDeposit(AccountPlan accountPlan) {
        double initialDeposit;
        do {
            try {
                Logger.warning("To go back to the main menu please enter -1");
                Logger.printInfo("Your deposit limit: " + accountRepository.getDepositLimit(accountPlan));
                System.out.println("Enter your desired deposit amount (e.g., 100.00): ");
                initialDeposit = scanner.nextDouble();
                scanner.nextLine();

                if (initialDeposit == -1) {
                    break;
                }
                accountRepository.validateDepositLimit(initialDeposit, accountPlan);
                break;

            } catch (Exception e) {
                Logger.error("Invalid account type!");
            }
        } while (true);
        return initialDeposit;
    }

    private void showDepositPrompt() {
        inputAccountPin();
        Logger.printInfo("Your account's deposit limit is: " + accountRepository.getDepositLimit(userAccount.getPlan()) + " USD.");
        System.out.println("Enter your desired deposit amount (e.g., 100.00): ");
        try {
            double amount = scanner.nextDouble();
            scanner.nextLine();
            if (amount <= 0.0) {
                throw new IllegalArgumentException("Invalid amount.");
            }
            userAccount.deposit(amount);
            accountRepository.logUserTransaction(userAccount, TransactionType.CREDIT, amount);
            Logger.printInfo("You have successfully deposited " + amount + " USD. Your new balance is: " + userAccount.getBalance() + " USD.");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void showWithdrawPrompt() {
        inputAccountPin();
        Logger.printInfo("Your account's withdrawal limit is: " + accountRepository.getWithdrawLimit(userAccount.getPlan()) + " USD.");
        System.out.println("Enter your desired withdrawal amount (e.g., 100.00): ");
        try {
            double amount = scanner.nextDouble();
            scanner.nextLine();
            if (amount <= 0.0) {
                throw new IllegalArgumentException("Invalid amount.");
            }
            userAccount.withdraw(amount);
            accountRepository.logUserTransaction(userAccount, TransactionType.DEBIT, amount);
            Logger.printInfo("You have successfully withdrawn " + amount + " USD. Your new balance is: " + userAccount.getBalance() + " USD.");
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
