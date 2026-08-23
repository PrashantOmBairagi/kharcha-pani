package com.prashant.kharchapaniapplication.financialmonth;

import com.prashant.kharchapaniapplication.enums.ExpenseCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryBreakdownResponse {

    private ExpenseCategory category;
    private BigDecimal total;
    private Double percentage;

}
