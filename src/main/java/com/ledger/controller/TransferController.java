package com.ledger.controller;
import com.ledger.dto.TransferRequest;
import com.ledger.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping("/transfers")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> transfer(@Valid @RequestBody TransferRequest request, org.springframework.security.core.Authentication auth) {
        transferService.transfer(auth.getName(), request);
        return Map.of("status", "Transfer completed successfully");
    }

    @GetMapping("/me/balance")
    public java.math.BigDecimal getBalance(org.springframework.security.core.Authentication auth) {
        return transferService.getBalance(auth.getName());
    }

}
