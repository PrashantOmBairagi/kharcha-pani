package com.prashant.kharchapaniapplication.expense;

import com.prashant.kharchapaniapplication.exception.ResourceNotFoundException;
import com.prashant.kharchapaniapplication.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public void createExpense(ExpenseRequest request , User currentUser) {
        Expense expense = new Expense();
        expense.setCategory(request.getCategory());
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setUser(currentUser);
        expenseRepository.save(expense);
    }

    public void updateExpense(UUID expenseId, ExpenseUpdateRequest request, User currentUser) {
        Expense expense = findExpenseById(expenseId, currentUser);
        expense.setCategory(request.getCategory());
        expense.setAmount(request.getAmount());
        expense.setDescription(request.getDescription());
        expense.setExpenseDate(request.getExpenseDate());
        expenseRepository.save(expense);
    }

    public Page findAllExpenses(Pageable pageable, User currentUser) {
        return expenseRepository.findByUserId(currentUser.getId(),pageable);
    }

    public ResponseEntity<String> deleteExpenseById(UUID id, User currentUser) {
            Expense expense =
                    expenseRepository
                        .findByIdAndUserId(
                                id,
                                currentUser.getId()
                        )
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Expense not found"
                                )
                        );
        expenseRepository.delete(expense);
        return new ResponseEntity<>("Expense deleted", HttpStatus.OK);
    }
    public Expense findExpenseById(UUID id, User currentUser) {
        return expenseRepository
                .findByIdAndUserId(
                        id,
                        currentUser.getId()
                )
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Expense not found"
                        )
                );
    }
}
