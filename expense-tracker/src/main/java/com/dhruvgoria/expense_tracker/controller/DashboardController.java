package com.dhruvgoria.expense_tracker.controller;

import com.dhruvgoria.expense_tracker.entity.Category;
import com.dhruvgoria.expense_tracker.entity.CategoryType;
import com.dhruvgoria.expense_tracker.entity.Transaction;
import com.dhruvgoria.expense_tracker.entity.User;
import com.dhruvgoria.expense_tracker.repository.CategoryRepository;
import com.dhruvgoria.expense_tracker.repository.TransactionRepository;
import com.dhruvgoria.expense_tracker.repository.UserRepository;
import com.dhruvgoria.expense_tracker.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BudgetService budgetService;

    // Extracted safely using your existing security core principal framework
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        User currentUser = getCurrentUser();
        // Updated from findAll() to fetch transactions isolated to the actual logged-in user account
        List<Transaction> transactions = transactionRepository.findByUser(currentUser);

        // Calculate Total Income
        double totalIncome = transactions.stream()
                .filter(t -> t.getType() == CategoryType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();

        // Calculate Total Expenses
        double totalExpense = transactions.stream()
                .filter(t -> t.getType() == CategoryType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();

        // Calculate Balance (Savings)
        double balance = totalIncome - totalExpense;

        // Calculate Savings Rate Percentage
        double savingsRate = totalIncome > 0 ? (balance / totalIncome) * 100 : 0.0;

        // --- Advanced Budget Tracking Module: Evaluate Category Spending Limits ---
        List<String> budgetAlerts = new ArrayList<>();
        List<Category> allCategories = categoryRepository.findAll();

        for (Category cat : allCategories) {
            if (budgetService.isOverBudget(currentUser, cat)) {
                budgetAlerts.add("Warning: You have blown past your target cap limit for " + cat.getName() + "!");
            }
        }

        // --- Integrated Recommendation Engine ---
        String recommendation;
        if (totalIncome == 0) {
            recommendation = "💡 Please log your salary or income sources to get financial recommendations.";
        } else if (balance < 0) {
            recommendation = "🚨 Warning: You are spending more than you earn! Check your category breakdown below and cut non-essential costs immediately.";
        } else if (!budgetAlerts.isEmpty()) {
            recommendation = "⚠️ Budget Alert: You have broken target cap ceilings on specific categories (like Food or Shopping). Review your target allowances immediately.";
        } else if (savingsRate < 20.0) {
            recommendation = "⚠️ Tight Budget: You saved " + String.format("%.1f", savingsRate) + "% of your income. Try to reduce leisure expenses to reach a healthy 20% savings goal.";
        } else {
            recommendation = "🎉 Great job! Your savings rate is " + String.format("%.1f", savingsRate) + "%. You have a healthy budget runway and room for investing or extra savings.";
        }

        // Combine metric properties into final payload map structure
        Map<String, Object> response = new HashMap<>();
        response.put("totalIncome", totalIncome);
        response.put("totalExpense", totalExpense);
        response.put("balance", balance);
        response.put("savingsRatePercentage", Math.round(savingsRate * 100.0) / 100.0);
        response.put("categoryAlerts", budgetAlerts); // Returns items like: ["Warning: You have blown past your target cap limit for Food!"]
        response.put("recommendation", recommendation);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/percentage-breakdown")
    public ResponseEntity<Map<String, Double>> getPercentageBreakdown() {
        User currentUser = getCurrentUser();
        List<Transaction> transactions = transactionRepository.findByUser(currentUser);

        List<Transaction> expenses = transactions.stream()
                .filter(t -> t.getType() == CategoryType.EXPENSE)
                .collect(Collectors.toList());

        double totalExpense = expenses.stream()
                .mapToDouble(Transaction::getAmount)
                .sum();

        if (totalExpense == 0) {
            return ResponseEntity.ok(Map.of());
        }

        Map<String, Double> percentageMap = expenses.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategory() != null ? t.getCategory().getName() : "Uncategorized",
                        Collectors.summingDouble(Transaction::getAmount)
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> Math.round((entry.getValue() / totalExpense) * 100.0 * 10.0) / 10.0
                ));

        return ResponseEntity.ok(percentageMap);
    }

    @GetMapping("/category-breakdown")
    public ResponseEntity<Map<String, Double>> getCategoryBreakdown() {
        User currentUser = getCurrentUser();
        // Updated to use user-specific scope rather than open database findAll() leaks
        List<Transaction> transactions = transactionRepository.findByUser(currentUser);

        Map<String, Double> breakdown = transactions.stream()
                .filter(t -> t.getType() == CategoryType.EXPENSE)
                .filter(t -> t.getCategory() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().getName(),
                        Collectors.summingDouble(Transaction::getAmount)
                ));

        return ResponseEntity.ok(breakdown);
    }
    @GetMapping("/monthly")
    public ResponseEntity<List<Map<String, Object>>> getMonthly() {
        List<Transaction> transactions = transactionRepository.findByUser(getCurrentUser());
        int year = java.time.LocalDate.now().getYear();
        List<Map<String, Object>> result = new ArrayList<>();

        for (int m = 1; m <= 12; m++) {
            final int month = m;
            double income = transactions.stream()
                    .filter(t -> t.getDate() != null && t.getDate().getYear() == year
                            && t.getDate().getMonthValue() == month
                            && t.getType() == CategoryType.INCOME)
                    .mapToDouble(Transaction::getAmount).sum();
            double expense = transactions.stream()
                    .filter(t -> t.getDate() != null && t.getDate().getYear() == year
                            && t.getDate().getMonthValue() == month
                            && t.getType() == CategoryType.EXPENSE)
                    .mapToDouble(Transaction::getAmount).sum();

            Map<String, Object> row = new HashMap<>();
            row.put("month", java.time.Month.of(m).getDisplayName(
                    java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH));
            row.put("income", income);
            row.put("expense", expense);
            result.add(row);
        }
        return ResponseEntity.ok(result);
    }
}