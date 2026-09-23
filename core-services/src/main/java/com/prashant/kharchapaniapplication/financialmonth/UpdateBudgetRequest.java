package com.prashant.kharchapaniapplication.financialmonth;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateBudgetRequest {

    @NotNull(message = "Budget is required.")
    @PositiveOrZero(message = "Budget cannot be negative.")
    private BigDecimal budget;
}
