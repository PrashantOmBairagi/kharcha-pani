package com.prashant.kharchapaniapplication.financialmonth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/fmonth")
public class FinancialMonthController {

    private final FinancialMonthService fMonthService;

    @PostMapping()
    public ResponseEntity<FinancialMonth> createFMonth(@Valid @RequestBody FinancialMonthRequest request) {
        return ResponseEntity.ok(fMonthService.createFMonth(request));
    }

    @PatchMapping("/{id}/budget")
    public ResponseEntity<FinancialMonth> updateBudget(@PathVariable UUID id, @Valid @RequestBody UpdateBudgetRequest request) {
        FinancialMonth updated = fMonthService.updateFMonthBudget(id, request.getBudget());
        return ResponseEntity.ok(updated);
    }
}
