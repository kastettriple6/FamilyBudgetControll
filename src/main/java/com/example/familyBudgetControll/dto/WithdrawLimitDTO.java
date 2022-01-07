package com.example.familyBudgetControll.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class WithdrawLimitDTO {
    private LocalDate dateForLimit;
    private Double limitForSingleWithdraw;
    private Double limitPerDay;
    private Double limitByDate;
}
