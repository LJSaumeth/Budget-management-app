package com.budgetapp.limits.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "budget_limit", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"budgetId", "period"})
})
@Getter
@Setter
@NoArgsConstructor
public class BudgetLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long budgetId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Period period;

    @Column(nullable = false)
    private int warningThresholdPercent = 80;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public BudgetLimit(Long budgetId, BigDecimal amount, Period period, int warningThresholdPercent) {
        this.budgetId = budgetId;
        this.amount = amount;
        this.period = period;
        this.warningThresholdPercent = warningThresholdPercent;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
