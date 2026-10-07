package com.dhruvgoria.expense_tracker.controller;
import com.dhruvgoria.expense_tracker.dto.BudgetRecommendation;
import com.dhruvgoria.expense_tracker.service.BudgetRecommendationService;
import com.dhruvgoria.expense_tracker.entity.Budget;
import com.dhruvgoria.expense_tracker.entity.Category;
import com.dhruvgoria.expense_tracker.entity.User;
import com.dhruvgoria.expense_tracker.repository.BudgetRepository;
import com.dhruvgoria.expense_tracker.repository.CategoryRepository;
import com.dhruvgoria.expense_tracker.repository.UserRepository;
import com.dhruvgoria.expense_tracker.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    @Autowired private BudgetService budgetService;
    @Autowired private BudgetRepository budgetRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public List<Map<String, Object>> getBudgets() {
        User user = getCurrentUser();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Budget b : budgetRepository.findByUser(user)) {
            double spent = budgetService.getAmountSpentExceedingBudget(user, b.getCategory());
            Map<String, Object> row = new HashMap<>();
            row.put("id", b.getId());
            row.put("categoryName", b.getCategory().getName());
            row.put("amountLimit", b.getAmountLimit());
            row.put("spent", spent);
            row.put("startDate", b.getStartDate());
            row.put("endDate", b.getEndDate());
            row.put("overBudget", spent > b.getAmountLimit());
            result.add(row);
        }
        return result;
    }
    @Autowired
    private BudgetRecommendationService budgetRecommendationService;

    @GetMapping("/recommendation/{categoryId}")
    public BudgetRecommendation getRecommendation(@PathVariable Long categoryId,
                                                  @RequestParam Long userId) {
        return budgetRecommendationService.checkBudgetStatus(categoryId, userId);
    }

    @PostMapping
    public Map<String, Object> setBudget(@RequestBody Map<String, Object> body) {
        User user = getCurrentUser();
        Category category = categoryRepository.findById(Long.valueOf(body.get("categoryId").toString()))
                .orElseThrow(() -> new RuntimeException("Category not found"));
        double limit = Double.parseDouble(body.get("amountLimit").toString());
        LocalDate start = LocalDate.parse(body.get("startDate").toString());
        LocalDate end = LocalDate.parse(body.get("endDate").toString());

        Budget saved = budgetService.setBudget(user, category, limit, start, end);
        return Map.of("id", saved.getId());
    }

    @DeleteMapping("/{id}")
    public void deleteBudget(@PathVariable Long id) {
        User user = getCurrentUser();
        budgetRepository.findById(id).ifPresent(b -> {
            if (b.getUser().getId().equals(user.getId())) budgetRepository.delete(b);
        });
    }
}