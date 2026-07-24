package com.ledger.dto;
import lombok.Data;
import com.ledger.model.TransactionType;
import java.math.BigDecimal;

@Data
public class TransactionRequest {
    private BigDecimal amount;
    private TransactionType type;
    private String description;
}
