package com.prashant.kharchapaniapplication.financialmonth;

import com.prashant.kharchapaniapplication.auth.AuthService;
import com.prashant.kharchapaniapplication.enums.ExpenseCategory;
import com.prashant.kharchapaniapplication.exception.ResourceNotFoundException;
import com.prashant.kharchapaniapplication.expense.Expense;
import com.prashant.kharchapaniapplication.expense.ExpenseResponse;
import com.prashant.kharchapaniapplication.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Transactional(readOnly = true)
    public FinancialMonthSummaryResponse getSummary(UUID fMonthId) {
        User currentUser = authService.getCurrentUser();
        FinancialMonth financialMonth = getFinancialMonthByIdAndUser(fMonthId, currentUser);
        return buildSummary(financialMonth);
    }

    @Transactional(readOnly = true)
    public FinancialMonthDetailResponse getDetail(UUID fMonthId, Pageable pageable) {
        User currentUser = authService.getCurrentUser();
        FinancialMonth financialMonth = getFinancialMonthByIdAndUser(fMonthId, currentUser);

        return new FinancialMonthDetailResponse(
                buildSummary(financialMonth),
                buildCategoryBreakdown(financialMonth),
                buildDailyTrend(financialMonth),
                buildRecentExpenses(financialMonth, pageable.getPageSize())
        );
    }

    @Transactional(readOnly = true)
    public FinancialMonthSummaryResponse getCurrentMonthSummary(User user) {
        LocalDate now = LocalDate.now();
        return financialMonthRepository.findByUserIdAndYearAndMonth(user.getId(), now.getYear(), now.getMonthValue())
                .map(this::buildSummary)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Financial month not found for " + now.getYear() + "-" + now.getMonthValue()));
    }

    @Transactional(readOnly = true)
    public FinancialMonthSummaryResponse getSummaryByYearMonth(User user, int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Invalid month: " + month);
        }
        return financialMonthRepository.findByUserIdAndYearAndMonth(user.getId(), year, month)
                .map(this::buildSummary)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Financial month not found for " + year + "-" + month));
    }

    @Transactional(readOnly = true)
    public Page<FinancialMonthSummaryResponse> getAllMonthsSummary(User user, Pageable pageable) {
        Pageable effectivePageable = pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "year", "month"));

        return financialMonthRepository.findByUserId(user.getId(), effectivePageable)
                .map(this::buildSummary);
    }

    private FinancialMonthSummaryResponse buildSummary(FinancialMonth financialMonth) {
        List<Expense> expenses = financialMonth.getExpenses();

        BigDecimal totalSpent = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDate lastExpenseDate = expenses.stream()
                .map(Expense::getExpenseDate)
                .max(LocalDate::compareTo)
                .orElse(null);

        return new FinancialMonthSummaryResponse(
                financialMonth.getId(),
                financialMonth.getYear(),
                financialMonth.getMonth(),
                financialMonth.getBudget(),
                financialMonth.getMonthlyIncome(),
                totalSpent,
                financialMonth.getBudget().subtract(totalSpent),
                expenses.size(),
                lastExpenseDate
        );
    }

    private List<CategoryBreakdownResponse> buildCategoryBreakdown(FinancialMonth financialMonth) {
        List<Expense> expenses = financialMonth.getExpenses();

        Map<ExpenseCategory, BigDecimal> totalsByCategory = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));

        BigDecimal totalSpent = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalsByCategory.entrySet().stream()
                .sorted(Map.Entry.<ExpenseCategory, BigDecimal>comparingByValue().reversed())
                .map(entry -> new CategoryBreakdownResponse(
                        entry.getKey(),
                        entry.getValue(),
                        calculatePercentage(entry.getValue(), totalSpent)
                ))
                .toList();
    }

    private List<DailyTrendResponse> buildDailyTrend(FinancialMonth financialMonth) {
        YearMonth yearMonth = YearMonth.of(financialMonth.getYear(), financialMonth.getMonth());

        Map<LocalDate, BigDecimal> totalsByDay = financialMonth.getExpenses().stream()
                .collect(Collectors.groupingBy(
                        Expense::getExpenseDate,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));

        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        if (yearMonth.equals(YearMonth.from(LocalDate.now()))) {
            end = LocalDate.now();
        }

        List<DailyTrendResponse> trend = new ArrayList<>();
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            trend.add(new DailyTrendResponse(day, totalsByDay.getOrDefault(day, BigDecimal.ZERO)));
        }
        return trend;
    }

    private List<ExpenseResponse> buildRecentExpenses(FinancialMonth financialMonth, int limit) {
        return financialMonth.getExpenses().stream()
                .sorted(Comparator.comparing(Expense::getExpenseDate, Comparator.reverseOrder())
                        .thenComparing(Expense::getCreatedAt, Comparator.reverseOrder()))
                .limit(limit)
                .map(expense -> new ExpenseResponse(
                        expense.getId(),
                        expense.getDescription(),
                        expense.getAmount(),
                        expense.getCategory(),
                        expense.getExpenseDate(),
                        expense.getUser().getId()
                ))
                .toList();
    }

    private Double calculatePercentage(BigDecimal categoryTotal, BigDecimal totalSpent) {
        if (totalSpent.signum() == 0) {
            return 0.0;
        }
        return categoryTotal
                .multiply(BigDecimal.valueOf(100))
                .divide(totalSpent, 1, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
