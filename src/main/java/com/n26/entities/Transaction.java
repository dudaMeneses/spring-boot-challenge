package com.n26.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Transaction {

    private BigDecimal amount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private LocalDateTime timestamp;

    public Transaction(BigDecimal amount, LocalDateTime timestamp) {
        this.amount = amount;
        this.timestamp = timestamp;
    }

}
