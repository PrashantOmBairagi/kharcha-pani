package com.prashant.kharchapaniapplication.expense;

import com.prashant.kharchapaniapplication.enums.ExpenseCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {

        private UUID id;
        private String description;
        private BigDecimal amount;
        private ExpenseCategory category;
        private LocalDate expenseDate;
        private UUID userId;

}
