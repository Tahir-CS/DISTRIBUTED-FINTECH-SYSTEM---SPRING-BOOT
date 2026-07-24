package com.ledger.dto;
import lombok.Data;
import com.ledger.model.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private TransactionType type;
    private String description;
    private LocalDateTime createdAt;
}
