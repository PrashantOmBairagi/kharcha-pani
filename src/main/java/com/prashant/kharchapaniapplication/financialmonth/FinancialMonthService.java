package com.prashant.kharchapaniapplication.financialmonth;

import com.prashant.kharchapaniapplication.auth.AuthService;
import com.prashant.kharchapaniapplication.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinancialMonthService {

    private final FinancialMonthRepository financialMonthRepository;
    private final AuthService authService;

    public FinancialMonth createFMonth(FinancialMonthRequest request) {
        User currentUser = authService.getCurrentUser();
        
        if (financialMonthRepository.findByUserIdAndYearAndMonth(currentUser.getId(), request.getYear(), request.getMonth()).isPresent()) {
            throw new IllegalArgumentException("Financial month for " + request.getYear() + "-" + request.getMonth() + " already exists");
        }
        
        FinancialMonth financialMonth = new FinancialMonth();
        financialMonth.setUser(currentUser);
        financialMonth.setBudget(request.getBudget());
        financialMonth.setMonthlyIncome(request.getMonthlyIncome());
        financialMonth.setMonth(request.getMonth());
        financialMonth.setYear(request.getYear());
        return financialMonthRepository.save(financialMonth);
    }

    public FinancialMonth getOrCreateCurrentMonth(User user) {
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        return financialMonthRepository.findByUserIdAndYearAndMonth(user.getId(), currentYear, currentMonth)
                .orElseGet(() -> createNewMonth(user, currentYear, currentMonth));
    }

    private FinancialMonth createNewMonth(User user, int year, int month) {
        BigDecimal previousBudget = getPreviousMonthBudget(user, year, month);

        FinancialMonth financialMonth = new FinancialMonth();
        financialMonth.setUser(user);
        financialMonth.setYear(year);
        financialMonth.setMonth(month);
        financialMonth.setBudget(previousBudget);
        financialMonth.setMonthlyIncome(BigDecimal.ZERO);
        return financialMonthRepository.save(financialMonth);
    }

    private BigDecimal getPreviousMonthBudget(User user, int year, int month) {
        LocalDate previousMonthDate = LocalDate.of(year, month, 1).minusMonths(1);
        return financialMonthRepository.findByUserIdAndYearAndMonth(
                        user.getId(), previousMonthDate.getYear(), previousMonthDate.getMonthValue())
                .map(FinancialMonth::getBudget)
                .orElse(BigDecimal.ZERO);
    }

    public FinancialMonth updateFMonthBudget(UUID fMonthId, BigDecimal budget) {
        return financialMonthRepository.findById(fMonthId)
                .map(financialMonth -> {
                    financialMonth.setBudget(budget);
                    return financialMonthRepository.save(financialMonth);
                })
                .orElseThrow(() -> new IllegalArgumentException("Financial month not found: " + fMonthId));
    }

    public FinancialMonth getFinancialMonthByIdAndUser(UUID fMonthId, User user) {
        return financialMonthRepository.findById(fMonthId)
                .filter(fm -> fm.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Financial month not found: " + fMonthId));
    }

    public FinancialMonth save(FinancialMonth financialMonth) {
        return financialMonthRepository.save(financialMonth);
    }
}
