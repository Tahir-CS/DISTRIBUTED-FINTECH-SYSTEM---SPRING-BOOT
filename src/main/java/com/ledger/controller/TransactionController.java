package com.ledger.controller;
import com.ledger.dto.TransactionRequest;
import com.ledger.dto.TransactionResponse;
import com.ledger.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse create(@RequestBody TransactionRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<TransactionResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public TransactionResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }
}
