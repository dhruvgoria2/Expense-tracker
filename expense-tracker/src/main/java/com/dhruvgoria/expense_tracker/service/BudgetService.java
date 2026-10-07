package com.dhruvgoria.expense_tracker.service;

import com.dhruvgoria.expense_tracker.entity.Budget;
import com.dhruvgoria.expense_tracker.entity.Category;
import com.dhruvgoria.expense_tracker.entity.Transaction;
import com.dhruvgoria.expense_tracker.entity.User;
import com.dhruvgoria.expense_tracker.repository.BudgetRepository;
import com.dhruvgoria.expense_tracker.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public Budget setBudget(User user, Category category, double limit, LocalDate start, LocalDate end) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Budget limit must be greater than zero");
        }

        Budget budget = budgetRepository.findByUserAndCategory(user, category)
                .orElse(new Budget());

        budget.setUser(user);
        budget.setCategory(category);
        budget.setAmountLimit(limit);
        budget.setStartDate(start);
        budget.setEndDate(end);

        return budgetRepository.save(budget);
    }
    public Budget getBudgetByCategory(Long categoryId, Long userId) {
        return budgetRepository.findByCategoryIdAndUserId(categoryId, userId).orElse(null);
    }

    public double getAmountSpentExceedingBudget(User user, Category category) {
        Optional<Budget> budgetOpt = budgetRepository.findByUserAndCategory(user, category);
        if (budgetOpt.isEmpty()) {
            return 0.0;
        }

        Budget budget = budgetOpt.get();

        List<Transaction> transactions = transactionRepository.findByUserIdAndDateBetween(
                user.getId(), budget.getStartDate(), budget.getEndDate()
                // If findByUserIdAndDateBetween throws an error, make sure it matches your TransactionRepository method!
        );

        return transactions.stream()
                .filter(t -> t.getType() == com.dhruvgoria.expense_tracker.entity.CategoryType.EXPENSE)
                .filter(t -> t.getCategory() != null && t.getCategory().getId().equals(category.getId()))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public boolean isOverBudget(User user, Category category) {
        Optional<Budget> budgetOpt = budgetRepository.findByUserAndCategory(user, category);
        if (budgetOpt.isEmpty()) {
            return false;
        }

        double currentSpend = getAmountSpentExceedingBudget(user, category);
        return currentSpend > budgetOpt.get().getAmountLimit();
    }
}