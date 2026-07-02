package com.prashant.smartfinancetracker.expense;

import com.prashant.smartfinancetracker.auth.AuthService;
import com.prashant.smartfinancetracker.user.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final AuthService authService;

    @PostMapping
    public ResponseEntity<?> createExpense(@Valid @RequestBody ExpenseRequest expenseRequest) {
        User currentUser = authService.getCurrentUser();
        expenseService.createExpense(expenseRequest, currentUser);
        return new ResponseEntity<>("Kharcha Saved!",HttpStatus.ACCEPTED);
    }

    @GetMapping
    public ExpensePageResponse getAllExpenses(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize
    ){
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        User currentUser = authService.getCurrentUser();

        Page<Expense> page = expenseService.findAllExpenses(pageable, currentUser);

        List<ExpenseResponse> expenses = page.getContent()
                .stream()
                .map(expense -> new ExpenseResponse(
                        expense.getId(),
                        expense.getDescription(),
                        expense.getAmount(),
                        expense.getCategory(),
                        expense.getExpenseDate(),
                        expense.getUser().getId()
                ))
                .toList();

        return new ExpensePageResponse(
                expenses,
                page.getNumber() + 1,
                page.getTotalPages(),
                page.getTotalElements(),
                page.hasNext(),
                page.hasPrevious()
        );
    }

    @GetMapping("/{id}")
    public ExpenseResponse getExpenseById(@NotNull @PathVariable long id)
    {
        User currentUser = authService.getCurrentUser();
        Expense expense = expenseService.findExpenseById(id,currentUser);
        return new ExpenseResponse(expense.getId(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getExpenseDate(),
                expense.getUser() != null ? expense.getUser().getId() : null);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateExpense(@NotNull @PathVariable Long id , @Valid @RequestBody ExpenseUpdateRequest request){
        User currentUser = authService.getCurrentUser();
        expenseService.updateExpense(id,request,currentUser);
        return new ResponseEntity<>("Expense Updated Successfully",HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteExpenseById(@NotNull @PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        return expenseService.deleteExpenseById(id,currentUser);
    }





}
