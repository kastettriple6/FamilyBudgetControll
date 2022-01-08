package com.example.familyBudgetControll.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Getter
@Setter
@Entity
public class WithdrawLimit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private LocalDate dateForLimit;
    private Double limitForSingleWithdraw;
    private Double limitPerDay;
    private Double limitByDate;

    @OneToOne(mappedBy = "limit")
    private Users user;

    @OneToOne(mappedBy = "limit")
    private Family family;

}
