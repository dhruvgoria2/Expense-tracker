package com.dhruvgoria.expense_tracker.repository;
import java.util.List;
import java.util.Optional;
import com.dhruvgoria.expense_tracker.entity.Budget;
import com.dhruvgoria.expense_tracker.entity.Category;
import com.dhruvgoria.expense_tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByUserAndCategory(User user, Category category);
    Optional<Budget> findByCategoryIdAndUserId(Long categoryId, Long userId);
    List<Budget> findByUser(User user);
}