package com.dhruvgoria.expense_tracker.controller;

import com.dhruvgoria.expense_tracker.entity.Transaction;
import com.dhruvgoria.expense_tracker.entity.User;
import com.dhruvgoria.expense_tracker.repository.TransactionRepository;
import com.dhruvgoria.expense_tracker.repository.UserRepository;
import com.dhruvgoria.expense_tracker.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    // Core helper method to extract the logged-in user securely from the JWT token
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PostMapping("/add")
    public ResponseEntity<Transaction> addTransaction(@RequestBody Transaction transaction) {
        // 🔑 Extracts the logged-in user from the active security context
        User currentUser = getCurrentUser();

        // 🔑 Passes both the payload data and user instance to your service layer logic
        Transaction savedTransaction = transactionService.logTransaction(transaction, currentUser);
        return ResponseEntity.ok(savedTransaction);
    }

    @GetMapping
    public List<Transaction> getAllTransactions() {
        User currentUser = getCurrentUser();
        return transactionRepository.findAll().stream()
                .filter(t -> t.getUser() != null && t.getUser().getId().equals(currentUser.getId()))
                .collect(Collectors.toList());
    }

    @GetMapping("/filter")
    public List<Transaction> filterTransactions(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String search
    ) {
        User currentUser = getCurrentUser();
        List<Transaction> transactions = transactionRepository.findByUser(currentUser);

        return transactions.stream()
                .filter(t -> category == null || (t.getCategory() != null && t.getCategory().getName().equalsIgnoreCase(category)))
                .filter(t -> {
                    if (startDate == null || startDate.isEmpty()) return true;
                    return !t.getDate().isBefore(LocalDate.parse(startDate));
                })
                .filter(t -> {
                    if (endDate == null || endDate.isEmpty()) return true;
                    return !t.getDate().isAfter(LocalDate.parse(endDate));
                })
                .filter(t -> search == null || (t.getDescription() != null && t.getDescription().toLowerCase().contains(search.toLowerCase())))
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable Long id, @RequestBody Transaction updated) {
        User currentUser = getCurrentUser();
        Transaction existing = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // Security Verification: Restrict edits to the owner of the record
        if (existing.getUser() == null || !existing.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Not authorized to edit this transaction");
        }

        // Map updated payload values over existing database entry
        existing.setAmount(updated.getAmount());
        existing.setDescription(updated.getDescription());
        existing.setDate(updated.getDate());
        existing.setType(updated.getType());
        existing.setCategory(updated.getCategory());

        Transaction savedTransaction = transactionRepository.save(existing);
        return ResponseEntity.ok(savedTransaction);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        Transaction existing = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // Security Verification: Restrict deletions to the owner of the record
        if (existing.getUser() == null || !existing.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Not authorized to delete this transaction");
        }

        transactionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}