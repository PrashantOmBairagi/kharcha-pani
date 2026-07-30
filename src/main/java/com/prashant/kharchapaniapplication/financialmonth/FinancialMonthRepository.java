package com.prashant.kharchapaniapplication.financialmonth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface FinancialMonthRepository extends JpaRepository<FinancialMonth, UUID> {

}
