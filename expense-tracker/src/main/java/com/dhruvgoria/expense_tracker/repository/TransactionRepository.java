package com.dhruvgoria.expense_tracker.repository;

import com.dhruvgoria.expense_tracker.entity.Transaction;
import com.dhruvgoria.expense_tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end);

    List<Transaction> findByUser(User user);

    @Query("SELECT SUM(t.amount) FROM Transaction t " +
            "WHERE t.category.id = :categoryId AND t.user.id = :userId")
    BigDecimal sumExpensesByCategory(@Param("categoryId") Long categoryId,
                                     @Param("userId") Long userId);
}