package com.prashant.kharchapaniapplication.financialmonth;

import com.prashant.kharchapaniapplication.auth.AuthService;
import com.prashant.kharchapaniapplication.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/fmonth")
public class FinancialMonthController {

    private final FinancialMonthService fMonthService;
    private final AuthService authService;

    @PostMapping()
    public ResponseEntity<FinancialMonth> createFMonth(@Valid @RequestBody FinancialMonthRequest request) {
        User currentUser = authService.getCurrentUser();
        FinancialMonth financialMonth = new FinancialMonth();
        financialMonth.setUser(currentUser);
        financialMonth.setBudget(request.getBudget());
        financialMonth.setMonthlyIncome(request.getMonthlyIncome());
        financialMonth.setMonth(request.getMonth());
        financialMonth.setYear(request.getYear());
        FinancialMonth saved = fMonthService.save(financialMonth);
        return ResponseEntity.ok(saved);
    }

    @PatchMapping("/{id}/budget")
    public ResponseEntity<FinancialMonth> updateBudget(@PathVariable UUID id, @Valid @RequestBody UpdateBudgetRequest request) {
        FinancialMonth updated = fMonthService.updateFMonthBudget(id, request.getBudget());
        return ResponseEntity.ok(updated);
    }
}
