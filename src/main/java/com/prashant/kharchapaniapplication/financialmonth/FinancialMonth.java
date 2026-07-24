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
    @NotNull
    @Min(2000)
    @Max(2050)
    private Integer year;

    @Column(nullable = false)
    @NotNull
    @Min(1)
    @Max(12)
    private Integer month;

    @NotNull
    @PositiveOrZero
    private BigDecimal budget;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User userId;

    @OneToMany(
            mappedBy = "financialMonth",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore
    private List<Expense> expenses = new ArrayList<>();



}
