package com.prashant.kharchapaniapplication.financialmonth;

import com.prashant.kharchapaniapplication.expense.Expense;
import lombok.RequiredArgsConstructor;
import org.aspectj.bridge.IMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/fmonth")
public class FinancialMonthController {

    private final FinancialMonthService fMonthService;

    @PostMapping()
    public ResponseEntity<Boolean> createFMonth(@RequestBody FinancialMonthRequest request) {
        Boolean status =  fMonthService.createFMonth(request);
        return ResponseEntity.ok(status);
    }
}
