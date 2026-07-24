package com.ledger.service;
import com.ledger.dto.TransactionRequest;
import com.ledger.dto.TransactionResponse;
import com.ledger.model.Transaction;
import com.ledger.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    public TransactionResponse create(TransactionRequest request) {
        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setDescription(request.getDescription());
        Transaction saved = repository.save(transaction);
        return toResponse(saved);
    }

    public List<TransactionResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TransactionResponse findById(Long id) {
        Transaction t = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + id));
        return toResponse(t);
    }

    private TransactionResponse toResponse(Transaction t) {
        TransactionResponse response = new TransactionResponse();
        response.setId(t.getId());
        response.setAmount(t.getAmount());
        response.setType(t.getType());
        response.setDescription(t.getDescription());
        response.setCreatedAt(t.getCreatedAt());
        return response;
    }
}
