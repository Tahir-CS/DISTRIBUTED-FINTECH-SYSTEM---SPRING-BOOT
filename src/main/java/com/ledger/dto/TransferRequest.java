package com.ledger.dto;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Data
public class TransferRequest {
    @NotNull(message = "Recipient Username is required")
    private String toUsername;

    @NotNull(message = "Amount is required")
    @Positive(message = "Transfer amount must be positive")
    private BigDecimal amount;

    @Size(max = 200, message = "Description too long")
    private String description;
}
