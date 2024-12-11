package org.example.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

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


    public Account() {
    }

    public abstract void withdraw(double amount);

    public void deposit(double amount) {
        if (amount > this.getDepositLimit())
            throw new IllegalArgumentException("deposit limit exceeds.");
        this.setBalance(amount + this.getBalance());
    }
}
