package com.example.familyBudgetControll.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WithdrawLimit {
    private Float limitForSingleWithdraw;
    private Float limitPerDay;
    private Float limitByDate;

    public WithdrawLimit(WithdrawLimit limit) {
    }
}
