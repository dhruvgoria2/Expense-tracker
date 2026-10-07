package com.dhruvgoria.expense_tracker.repository;

import com.dhruvgoria.expense_tracker.entity.BankAccount;
import com.dhruvgoria.expense_tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    List<BankAccount> findByUser(User user);
    Optional<BankAccount> findByIdAndUser(Long id, User user);
}