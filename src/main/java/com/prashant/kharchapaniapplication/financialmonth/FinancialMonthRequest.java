package com.prashant.kharchapaniapplication.financialmonth;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class FinancialMonthRequest {


    @NotNull(message = "Budget can't be empty.")
    @PositiveOrZero(message = "Budget cannot be negative.")
    private BigDecimal budget;

    @PositiveOrZero(message = "Monthly Income cannot be negative.")
    private BigDecimal monthlyIncome;

    @NotNull(message = "Year can't be empty.")
    @Min(value = 2000,message = "Application currently support only years after 2000.")
    @Max(value = 2050,message = "Application currently support years up to 2050.")
    private Integer year;

    @NotNull(message = "Month can't be empty.")
    @Min(value = 1,message = "Month cannot be less than 1.")
    @Max(value = 12,message = "Month cannot be more than 12.")
    private Integer month;


}
