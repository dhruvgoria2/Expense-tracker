package com.dhruvgoria.expense_tracker.service;

import com.dhruvgoria.expense_tracker.entity.Budget;
import com.dhruvgoria.expense_tracker.dto.BudgetRecommendation;
import com.dhruvgoria.expense_tracker.repository.TransactionRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

@Service
public class BudgetRecommendationService {

    private final TransactionRepository transactionRepository;
    private final BudgetService budgetService;

    public BudgetRecommendationService(
            TransactionRepository transactionRepository,
            BudgetService budgetService) {

        this.transactionRepository = transactionRepository;
        this.budgetService = budgetService;
    }

    public BudgetRecommendation checkBudgetStatus(Long categoryId, Long userId) {

        // Get total expenses for the category
        BigDecimal totalExpense =
                transactionRepository.sumExpensesByCategory(categoryId, userId);

        // Get budget for the category
        Budget budget =
                budgetService.getBudgetByCategory(categoryId, userId);

        BudgetRecommendation recommendation =
                new BudgetRecommendation();

        // No expense or no budget found
        if (totalExpense == null || budget == null) {
            recommendation.setStatus("NO_DATA");
            recommendation.setMessage("No budget or expense data found.");
            recommendation.setPercentageUsed(BigDecimal.ZERO);
            recommendation.setTotalExpense(
                    totalExpense != null ? totalExpense : BigDecimal.ZERO
            );

            if (budget != null) {
                recommendation.setBudgetLimit(BigDecimal.valueOf(budget.getAmountLimit()));
            } else {
                recommendation.setBudgetLimit(BigDecimal.ZERO);
            }

            recommendation.setRecommendations(
                    Arrays.asList(
                            "Set a budget for this category",
                            "Add some transactions to track spending"
                    )
            );

            return recommendation;
        }

        BigDecimal budgetLimit = BigDecimal.valueOf(budget.getAmountLimit());

        // Avoid division by zero
        if (budgetLimit == null || budgetLimit.compareTo(BigDecimal.ZERO) <= 0) {

            recommendation.setStatus("INVALID");
            recommendation.setMessage("Budget limit must be greater than zero.");
            recommendation.setPercentageUsed(BigDecimal.ZERO);
            recommendation.setTotalExpense(totalExpense);
            recommendation.setBudgetLimit(
                    budgetLimit != null ? budgetLimit : BigDecimal.ZERO
            );

            return recommendation;
        }

        // Calculate percentage of budget used
        BigDecimal percentage = totalExpense
                .divide(budgetLimit, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        recommendation.setPercentageUsed(percentage);
        recommendation.setTotalExpense(totalExpense);
        recommendation.setBudgetLimit(budgetLimit);

        // Generate recommendations
        if (percentage.compareTo(BigDecimal.valueOf(100)) > 0) {

            recommendation.setStatus("EXCEEDED");

            recommendation.setMessage(
                    "⚠️ Budget exceeded! Strictly save money."
            );

            recommendation.setRecommendations(
                    Arrays.asList(
                            "Stop unnecessary spending",
                            "Review recent transactions",
                            "Cut non-essential expenses"
                    )
            );

        } else if (percentage.compareTo(BigDecimal.valueOf(80)) > 0) {

            recommendation.setStatus("WARNING");

            recommendation.setMessage(
                    "⚠️ Budget nearly full (80%+). Start saving!"
            );

            recommendation.setRecommendations(
                    Arrays.asList(
                            "Reduce spending",
                            "Plan ahead for next month"
                    )
            );

        } else {

            recommendation.setStatus("SAFE");

            recommendation.setMessage(
                    "✅ You're within budget"
            );

            recommendation.setRecommendations(
                    Arrays.asList(
                            "Keep maintaining your spending",
                            "Continue tracking your expenses"
                    )
            );
        }

        return recommendation;
    }
}