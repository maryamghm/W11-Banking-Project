package my.bank.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Transaction {
    @JsonProperty("accountNumber")
    private Integer accountNumber;

    @JsonProperty("timeStamp")
    private LocalDateTime timestamp;

    @JsonProperty("type")
    private TransactionType type;

    @JsonProperty("amount")
    private double amount;

    public Transaction() {
    }
}
