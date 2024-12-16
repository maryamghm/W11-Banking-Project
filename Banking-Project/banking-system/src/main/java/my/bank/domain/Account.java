package my.bank.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CheckingAccount.class, name = "CHECKING_ACCOUNT"),
        @JsonSubTypes.Type(value = SavingsAccount.class, name = "SAVINGS_ACCOUNT")
})

public abstract class Account {
    @JsonProperty("accountNumber")
    private Integer accountNumber;

    @JsonProperty("pin")
    private String pin;

    @JsonProperty("userId")
    private Integer userId;

    @JsonProperty("type")
    private AccountType type;

    @JsonProperty("plan")
    private AccountPlan plan;

    @JsonProperty("balance")
    private Double balance;

    @JsonProperty("withdrawLimit")
    private Double withdrawLimit;

    @JsonProperty("depositLimit")
    private Double depositLimit;

    @JsonProperty("isActive")
    private boolean isActive;

    @JsonProperty("overdraftCounter")
    private int overdraftCounter;

    @JsonProperty("favoriteAccounts")
    private List<Integer> favoriteAccounts;


    @JsonIgnore
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Account() {
        this.favoriteAccounts = new ArrayList<>();
    }

    public abstract void withdraw(double amount);

    public void deposit(double amount) {
        if (amount > this.getDepositLimit())
            throw new IllegalArgumentException("deposit limit exceeds.");
        this.setBalance(amount + this.getBalance());
    }

    public void setNewPin(String pin) {
        if (pin == null || pin.isBlank()) {
            throw new IllegalArgumentException("Pin cannot be blank");
        }
        if (!pin.matches("^\\d{4}$")) {
            throw new IllegalArgumentException("Invalid pin format! Only 4 digits.");
        }
        this.pin = PasswordHashing.hashPasswordSHA1(pin);
    }

    public void validatePin(String pin) {
        if (!PasswordHashing.hashPasswordSHA1(pin).equals(this.pin)) {
            throw new IllegalArgumentException("Pin doesn't match");
        }
    }


}
