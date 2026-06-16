package com.budgetapp.limits.infrastructure.persistence;

import com.budgetapp.limits.domain.BudgetLimit;
import com.budgetapp.limits.domain.Period;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetLimitRepository extends JpaRepository<BudgetLimit, Long> {

    Optional<BudgetLimit> findByBudgetIdAndPeriod(Long budgetId, Period period);

    List<BudgetLimit> findByBudgetId(Long budgetId);
}
