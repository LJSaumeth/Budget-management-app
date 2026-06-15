package com.budgetapp.budgetcore.infrastructure.persistence;

import com.budgetapp.budgetcore.domain.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
}
