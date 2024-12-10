package org.example;

import org.example.domain.AccountPlan;
import org.example.domain.AccountType;
import org.example.domain.User;
import org.example.repository.AccountRepository;
import org.example.repository.UserRepository;

import java.io.File;
import java.util.Arrays;
import java.util.Scanner;

public class ConsoleApplication {
    private UserRepository userRepository;
    private AccountRepository accountRepository;
    private final Scanner scanner = new Scanner(System.in);
    private User loggedInUser = null;

    public void run() {
        userRepository = new UserRepository(new File("users.csv"));
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
                    return false;
                }
                default -> System.out.println("Choose 1-3: ");
            }
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
        }
        return true;
    }

    private boolean showEmployeeMenu() {
        return true;
    }

    private boolean showCustomerMenu() {
        System.out.println("Menu For Customers: ");
        System.out.println("1. Show Balance");
        System.out.println("2. Deposit");
        System.out.println("3. Withdraw");
        System.out.println("4. Reset Password");
        System.out.println("5. Logout");
        try {
            int choice = scanner.nextInt();
            scanner.nextLine();
            switch (choice) {
                case 5 -> {
                    userRepository.logout(loggedInUser.getUsername());
                    loggedInUser = null;
                }
                default -> System.out.println("Choose 1-5: ");
            }
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
        }
        return true;
    }

    private void showLoginPrompt() {
        System.out.println("Please enter your username: ");
        String username = scanner.nextLine();

        System.out.println("Please enter your password: ");
        String password = scanner.nextLine();

        loggedInUser = userRepository.login(username, password);
    }

    private void showSignupPrompt() {
        System.out.println("Please enter your username: ");
        String username = scanner.nextLine();

        System.out.println("Please enter your password: ");
        String password = scanner.nextLine();

        User user = userRepository.signUp(username, password);

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

        isInputValid = true;
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

        accountRepository = new AccountRepository();
        accountRepository.addNewAccount(user.getId(), AccountType.values()[type], AccountPlan.values()[accountPlan]);
        showLoginPrompt();
    }
}
