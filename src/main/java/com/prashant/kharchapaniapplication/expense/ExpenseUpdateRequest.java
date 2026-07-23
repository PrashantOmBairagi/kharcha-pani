package com.prashant.kharchapaniapplication.expense;

import com.prashant.kharchapaniapplication.enums.ExpenseCategory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.time.LocalDate;
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ExpenseUpdateRequest {

    @Length(max = 100)
    private String description;

    @NotNull
    @Positive(message = "Amount must be positive.")
    private BigDecimal amount;

    @NotNull
    private ExpenseCategory category ;

    @NotNull
    @PastOrPresent(message = "Expense cannot have future date.")
    private LocalDate expenseDate;
}
