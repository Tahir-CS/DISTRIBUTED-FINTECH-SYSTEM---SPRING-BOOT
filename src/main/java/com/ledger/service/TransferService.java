package com.ledger.service;
import com.ledger.dto.TransferRequest;
import com.ledger.exception.InsufficientFundsException;
import com.ledger.exception.SelfTransferException;
import com.ledger.model.User;
import com.ledger.model.Transaction;
import com.ledger.model.TransactionType;
import com.ledger.repository.TransactionRepository;
import com.ledger.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    public TransferService(UserRepository userRepository,
                           TransactionRepository transactionRepository,
                           org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public java.math.BigDecimal getBalance(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getBalance();
    }

    @Transactional
    public void transfer(String fromUsername, TransferRequest request) {

        if (fromUsername.equals(request.getToUsername())) {
            throw new SelfTransferException("Cannot transfer money to yourself");
        }

        Long senderId = userRepository.findIdByUsername(fromUsername)
                .orElseThrow(() -> new com.ledger.exception.UserNotFoundException("Sender not found"));

        Long recipientId = userRepository.findIdByUsername(request.getToUsername())
                .orElseThrow(() -> new com.ledger.exception.UserNotFoundException("Recipient not found"));

        // DEADLOCK PREVENTION: Always lock rows in the exact same order (by ID)
        User sender, recipient;
        if (senderId < recipientId) {
            sender = userRepository.findById(senderId).get();
            recipient = userRepository.findById(recipientId).get();
        } else {
            recipient = userRepository.findById(recipientId).get();
            sender = userRepository.findById(senderId).get();
        }

        if (sender.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Sender does not have enough balance");
        }

        // VULNERABLE READ-MODIFY-WRITE (Lost Update for Sender)
        sender.setBalance(sender.getBalance().subtract(request.getAmount()));
        userRepository.save(sender);

        // ATOMIC SQL UPDATE (Bob gets all the money)
        userRepository.addBalance(recipient.getId(), request.getAmount());

        Transaction debit = new Transaction();
        debit.setOwnerUsername(sender.getUsername());
        debit.setAmount(request.getAmount());
        debit.setType(TransactionType.EXPENSE);
        debit.setDescription("Transfer to " + recipient.getUsername() + ": " + request.getDescription());
        transactionRepository.save(debit);

        Transaction credit = new Transaction();
        credit.setOwnerUsername(recipient.getUsername());
        credit.setAmount(request.getAmount());
        credit.setType(TransactionType.INCOME);
        credit.setDescription("Transfer from " + sender.getUsername() + ": " + request.getDescription());
        transactionRepository.save(credit);

        // DEMO FEATURE: Simulate a server crash to prove @Transactional rolls back the database!
        if ("CRASH".equals(request.getDescription())) {
            throw new RuntimeException("Simulated Server Crash in the middle of a transfer!");
        }

        // Broadcast balance update events via WebSocket
        messagingTemplate.convertAndSend("/topic/balance/" + recipient.getUsername(), "UPDATE");
        messagingTemplate.convertAndSend("/topic/balance/" + sender.getUsername(), "UPDATE");
    }
}
