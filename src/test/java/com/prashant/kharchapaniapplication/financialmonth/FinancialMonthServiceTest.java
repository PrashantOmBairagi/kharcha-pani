package com.prashant.kharchapaniapplication.financialmonth;

import com.prashant.kharchapaniapplication.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancialMonthServiceTest {

    @Mock
    FinancialMonthRepository fMonthRepository;

    User user = new User();


    @ParameterizedTest
    @CsvSource({
            "5000,40000,2002,12",
            "7000,8000,2026,9"
    }
    )
    void testCreateFMonth(BigDecimal budget, BigDecimal monthlyIncome, int year, int month) {
        FinancialMonth request = new FinancialMonth();
        request.setBudget(budget);
        request.setMonthlyIncome(monthlyIncome);
        request.setYear(year);
        request.setMonth(month);
        request.setUser(user);


        when(fMonthRepository.save(any(FinancialMonth.class))).thenReturn(request);
        FinancialMonth result = fMonthRepository.save(request);


        assertNotNull(result);
        assertEquals(month, result.getMonth());
        assertEquals(year, result.getYear());
    }

    @ParameterizedTest
    @CsvSource({
            "18a33460-96e7-41be-9d99-5f17b9402d83"
    })
    void shouldGetFinancialMonthByIdAndUser(UUID fMonthId ) {


        Optional <FinancialMonth> fMonth = fMonthRepository.findById(fMonthId);

        assertTrue(fMonth.isPresent());
        assertEquals(fMonthId , fMonth.get().getId());
    }
}