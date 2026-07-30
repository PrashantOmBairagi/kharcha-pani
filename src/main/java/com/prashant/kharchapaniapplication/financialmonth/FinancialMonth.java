package com.prashant.kharchapaniapplication.financialmonth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.prashant.kharchapaniapplication.expense.Expense;
import com.prashant.kharchapaniapplication.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Getter
@Setter
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_year_month",
                        columnNames = {"userId", "year", "month"}
                )
        }
)
public class FinancialMonth {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal budget;

    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal monthlyIncome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(
            mappedBy = "financialMonth",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore
    private List<Expense> expenses = new ArrayList<>();



}
