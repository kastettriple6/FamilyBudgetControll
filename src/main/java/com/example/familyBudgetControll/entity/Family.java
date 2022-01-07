package com.example.familyBudgetControll.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Entity
public class Family {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column
    private String name;
    @Column
    private Double balance;
    @Column
    private Double sumOfWithdrawsByDay;

    @OneToOne
    @JoinColumn(name = "limit_id")
    private WithdrawLimit limit;

    @OneToMany(mappedBy = "family")
    private List<User> users;
}
