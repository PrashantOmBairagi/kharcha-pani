package com.prashant.kharchapaniapplication.financialmonth;

import com.prashant.kharchapaniapplication.auth.AuthService;
import com.prashant.kharchapaniapplication.expense.ExpenseResponse;
import com.prashant.kharchapaniapplication.expense.ExpenseService;
import com.prashant.kharchapaniapplication.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fmonth")
public class FinancialMonthController {

    private final FinancialMonthService fMonthService;
    private final AuthService authService;
    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<FinancialMonthSummaryResponse> createFMonth(@Valid @RequestBody FinancialMonthRequest request) {
        FinancialMonth financialMonth = fMonthService.createFMonth(request);
        FinancialMonthSummaryResponse response = fMonthService.getSummary(financialMonth.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/budget")
    public ResponseEntity<FinancialMonthSummaryResponse> updateBudget(@PathVariable UUID id, @Valid @RequestBody UpdateBudgetRequest request) {
        User currentUser = authService.getCurrentUser();
        FinancialMonth saved = fMonthService.updateFMonthBudget(id, currentUser, request.getBudget());
        FinancialMonthSummaryResponse response = fMonthService.getSummary(saved.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/current")
    public ResponseEntity<FinancialMonthSummaryResponse> getCurrentMonth() {
        User currentUser = authService.getCurrentUser();
        FinancialMonthSummaryResponse response = fMonthService.getCurrentMonthSummary(currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-date")
    public ResponseEntity<FinancialMonthSummaryResponse> getMonthByYearMonth(
            @RequestParam int year,
            @RequestParam int month
    ) {
        User currentUser = authService.getCurrentUser();
        FinancialMonthSummaryResponse response = fMonthService.getSummaryByYearMonth(currentUser, year, month);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list")
    public ResponseEntity<Page<FinancialMonthSummaryResponse>> getAllMonths(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        User currentUser = authService.getCurrentUser();
        int safePageSize = Math.min(pageSize, 50);
        Pageable pageable = PageRequest.of(pageNo - 1, safePageSize, Sort.by(Sort.Direction.DESC, "year", "month"));
        Page<FinancialMonthSummaryResponse> page = fMonthService.getAllMonthsSummary(currentUser, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}/expenses")
    public ResponseEntity<Page<ExpenseResponse>> getMonthExpenses(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        User currentUser = authService.getCurrentUser();
        FinancialMonth financialMonth = fMonthService.getFinancialMonthByIdAndUser(id, currentUser);

        int safePageSize = Math.min(pageSize, 50);
        Pageable pageable = PageRequest.of(pageNo - 1, safePageSize, Sort.by(Sort.Direction.DESC, "expenseDate"));

        Page<com.prashant.kharchapaniapplication.expense.Expense> expensePage = expenseService.findAllExpenses(pageable, currentUser, financialMonth.getId());

        Page<ExpenseResponse> responsePage = expensePage.map(expense -> new ExpenseResponse(
                expense.getId(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getExpenseDate(),
                expense.getUser().getId()
        ));

        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<FinancialMonthDetailResponse> getMonthDetail(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        User currentUser = authService.getCurrentUser();
        FinancialMonth financialMonth = fMonthService.getFinancialMonthByIdAndUser(id, currentUser);

        int safePageSize = Math.min(pageSize, 50);
        Pageable pageable = PageRequest.of(pageNo - 1, safePageSize, Sort.by(Sort.Direction.DESC, "expenseDate"));

        FinancialMonthDetailResponse response = fMonthService.getDetail(financialMonth.getId(), pageable);
        return ResponseEntity.ok(response);
    }
}