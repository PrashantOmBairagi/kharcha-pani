package com.prashant.kharchapaniapplication.financialmonth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailyTrendResponse {

    private LocalDate date;
    private BigDecimal total;

}
