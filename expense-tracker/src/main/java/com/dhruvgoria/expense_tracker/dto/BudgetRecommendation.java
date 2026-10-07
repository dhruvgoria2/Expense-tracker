package com.dhruvgoria.expense_tracker.dto;

import java.math.BigDecimal;
import java.util.List;
import com.dhruvgoria.expense_tracker.dto.BudgetRecommendation;

public class BudgetRecommendation {
    private String status;
    private String message;
    private BigDecimal percentageUsed;
    private BigDecimal totalExpense;
    private BigDecimal budgetLimit;
    private List<String> recommendations;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public BigDecimal getPercentageUsed() { return percentageUsed; }
    public void setPercentageUsed(BigDecimal percentageUsed) { this.percentageUsed = percentageUsed; }

    public BigDecimal getTotalExpense() { return totalExpense; }
    public void setTotalExpense(BigDecimal totalExpense) { this.totalExpense = totalExpense; }

    public BigDecimal getBudgetLimit() { return budgetLimit; }
    public void setBudgetLimit(BigDecimal budgetLimit) { this.budgetLimit = budgetLimit; }

    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
}