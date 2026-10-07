package com.dhruvgoria.expense_tracker.controller;

import com.dhruvgoria.expense_tracker.entity.BankAccount;
import com.dhruvgoria.expense_tracker.entity.User;
import com.dhruvgoria.expense_tracker.repository.BankAccountRepository;
import com.dhruvgoria.expense_tracker.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bank-accounts")
public class BankAccountController {

    private final BankAccountRepository repo;
    private final UserRepository userRepo;

    public BankAccountController(BankAccountRepository repo, UserRepository userRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
    }

    private User currentUser(Authentication auth) {
        return userRepo.findByEmail(auth.getName()).orElseThrow();
    }

    @GetMapping
    public List<BankAccount> getAll(Authentication auth) {
        return repo.findByUser(currentUser(auth));
    }

    @PostMapping
    public BankAccount create(@RequestBody BankAccount acc, Authentication auth) {
        acc.setId(null);
        acc.setUser(currentUser(auth));
        return repo.save(acc);
    }

    @PutMapping("/{id}")
    public BankAccount update(@PathVariable Long id, @RequestBody BankAccount body, Authentication auth) {
        BankAccount acc = repo.findByIdAndUser(id, currentUser(auth)).orElseThrow();
        acc.setBankName(body.getBankName());
        acc.setAccountType(body.getAccountType());
        acc.setAccountNumber(body.getAccountNumber());
        acc.setBalance(body.getBalance());
        return repo.save(acc);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication auth) {
        repo.delete(repo.findByIdAndUser(id, currentUser(auth)).orElseThrow());
    }
}