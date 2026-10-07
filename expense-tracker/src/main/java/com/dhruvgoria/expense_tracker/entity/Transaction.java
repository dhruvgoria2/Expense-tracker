package com.dhruvgoria.expense_tracker.entity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity

public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private double amount;
    private String description;
    private LocalDate date;
    @Enumerated(EnumType.STRING)
    private CategoryType type;
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    public Transaction() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public CategoryType getType() { return type; }
    public void setType(CategoryType type) { this.type = type; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public void setTimestamp(LocalDateTime now) {

    }
}
