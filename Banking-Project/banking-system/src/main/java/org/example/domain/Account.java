package org.example.domain;

import lombok.Data;

@Data
public abstract class Account {
    private String accountNumber;
    private Integer userId;
    private Double balance;
    private Double withdrawLimit;
    private AccountPlan plan;

    public Account() {
    }

    public abstract void withdraw();

    public abstract void deposit();
}
