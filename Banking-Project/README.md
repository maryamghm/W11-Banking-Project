

# **Java Banking Application Project Instructions**

## **Overview**
This project involves creating a command-line banking application in Java. The application should support user account management, basic banking operations, and advanced features for enhanced functionality. Follow the guidelines below to structure your application.

---

## **Project Flow**

### **1. Application Start**
- Present the user with the following menu options:
  ```
  1. Signup
  2. Login
  3. Exit
  ```

---

### **2. Signup Flow**
- **Inputs Required**:
  - **Username**: Must be unique and lowercase.
  - **Password**: Minimum of 6 characters.
  - **First Name**
  - **Initial Deposit Amount**
  - **Withdraw Limit**

- **Conditions**:
  - Validate all input fields.
  - If validation succeeds, create a new user and account.
  - Prompt the user to log in after successful signup.

---

### **3. Login Flow**
- **Inputs Required**:
  - **Username**
  - **Password**

- **Conditions**:
  - If the username or password is incorrect, display a meaningful error message.
  - Upon successful login, display the **Logged-In User Menu**.

---

## **Logged-In User Menu**

1. **Show Balance**: 
   - Display the user's current balance and return to the menu.

2. **Deposit**: 
   - Allow users to deposit money up to their specified deposit limit.

3. **Withdraw**: 
   - Allow users to withdraw money up to their specified withdraw limit.

4. **Reset Password**: 
   - Allow users to reset their password by verifying their original password.

5. **Logout**: 
   - Log the user out and return to the main menu.

---



## Basic Upgrades:
1. **Additional Validation**:
   - **Username must be lowercase**.
   - **Usernames must be unique**.
   - **Password must be at least 6 characters**.

2. **Type Validation**:
   - Ensure correct data types for **int**, **String**, **double** inputs before assigning.

3. **Universal Account Limits**:
   - Set consistent deposit and withdrawal limits across the system.

4. **PIN Code Support**:
   - Require PIN entry for every user request.

5. **Account Transfers**:
   - Allow transfers between accounts using account IDs.
   - Display the recipient's name during the transfer process.

6. **Savings Account Implementation**:
   - Create a **SavingsAccount** class inheriting from an abstract **Account** class.
   - **SavingsAccount**: No overdrafts allowed.
   - **CheckingAccount**: Overdrafts allowed.

---

## Advanced Upgrades

1. **Account Types**:
   - **Platinum Account**:
     - Withdraw Limit: $20,000
     - Deposit Limit: $30,000
   - **Gold Account**:
     - Withdraw Limit: $10,000
     - Deposit Limit: $15,000
   - **Silver Account**:
     - Withdraw Limit: $1,000
     - Deposit Limit: $1,500

2. **Overdraft Functionality**:
   - Allow users to overdraft twice.
   - Apply a $50 penalty for each overdraft.
   - Example:
     - **Current balance: $10**, withdrawal of **$20** → new balance: **-$60** (-$20 withdrawal, -$50 fee).
     - On second overdraft, apply another fee.
     - Prevent further withdrawals after the second overdraft.

3. **Lockout on Failed Logins**:
   - Lock the user out after a specified number of incorrect password attempts.

4. **Favorite Feature**:
   - Allow users to mark other users as favorites.
   - When transferring funds, show the list of favorites before prompting for an account ID.

5. **Account Deactivation**:
   - Add a boolean field `isActive` to the `User` class.
   - When a user deactivates their account, set `isActive` to `false`.
   - Prevent deactivated users from logging in.

6. **Support for Multiple Currencies**:
   - Implement support for multiple currencies in the application (e.g., USD, EUR).
   - Allow users to specify their preferred currency during signup.
   - Include currency conversion for transfers if necessary.

7. **Transaction History**:
   - Maintain a history of all transactions for each user account.
   - Display the complete transaction history upon user request.

8. **Transaction History by Date Range**:
   - Allow users to filter and view their transaction history within a specified date range.

9. **Password Encryption**:
   - Encrypt user passwords before storing them.
   - Ensure passwords are securely hashed using a library like **Bcrypt** or **Argon2**.

10. **User Roles**:
    - Add support for roles:
      - **Banker Role**: Access additional features like viewing all accounts, resetting user passwords, etc.
      - **User Role**: Standard access to account management features.
    - Provide different menu options and UI outputs for each role.

11. **Enhanced UI/UX**:
    - Make the CLI interface more user-friendly by:
      - Adding clear instructions at each step.
      - Using colored text to indicate warnings, errors, or success messages.
