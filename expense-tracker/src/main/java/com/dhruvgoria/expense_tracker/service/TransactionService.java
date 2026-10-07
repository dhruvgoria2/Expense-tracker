package com.dhruvgoria.expense_tracker.service;

import com.dhruvgoria.expense_tracker.entity.Transaction;
import com.dhruvgoria.expense_tracker.entity.User;
import com.dhruvgoria.expense_tracker.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    // 🔑 Updated to accept BOTH the transaction and the logged-in user
    public Transaction logTransaction(Transaction transaction, User currentUser) {
        if (transaction.getAmount() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        transaction.setDate(LocalDate.now());

        // 🔑 Binds the transaction to the user account in the database
        transaction.setUser(currentUser);

        return transactionRepository.save(transaction);
    }

    public List<Transaction> getTransactionsByPeriod(Long userId, LocalDate start, LocalDate end) {
        return transactionRepository.findByUserIdAndDateBetween(userId, start, end);
    }
}