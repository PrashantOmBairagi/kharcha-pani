package com.prashant.kharchapaniapplication.financialmonth;

import com.prashant.kharchapaniapplication.expense.ExpenseResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FinancialMonthDetailResponse {

    private FinancialMonthSummaryResponse summary;
    private List<CategoryBreakdownResponse> categoryBreakdown;
    private List<DailyTrendResponse> dailyTrend;
    private List<ExpenseResponse> recentExpenses;

}
