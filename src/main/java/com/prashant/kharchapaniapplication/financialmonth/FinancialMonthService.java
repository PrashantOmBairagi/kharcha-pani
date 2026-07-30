package com.prashant.kharchapaniapplication.financialmonth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinancialMonthService {

    private final FinancialMonthRepository financialMonthRepository;

    public Boolean createFMonth (FinancialMonthRequest request) {

        return true;
    }
}
