package com.prashant.kharchapaniapplication.financialmonth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FinancialMonthSummaryResponse {

    private UUID id;
    private int year;
    private int month;
    private BigDecimal budget;
    private BigDecimal monthlyIncome;
    private BigDecimal totalSpent;
    private BigDecimal remaining;
    private long expenseCount;
    private LocalDate lastExpenseDate;

}
