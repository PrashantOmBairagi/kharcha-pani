package com.prashant.kharchapaniapplication.expense;

import com.prashant.kharchapaniapplication.enums.ExpenseCategory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class ExpenseRequest {

    private String description;

    @NotNull(message = "Category is required.")
    private ExpenseCategory category ;

    @NotNull
    @Positive(message = "Amount must be positive.")
    private BigDecimal amount;

    @NotNull(message = "Expense date is required.")
    @PastOrPresent(message = "Expense cannot have future date.")
    private LocalDate expenseDate;

    private UUID financialMonthId;
}
