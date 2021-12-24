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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Float balance;
    private WithdrawLimit limit;

    @OneToMany(mappedBy = "family")
    private List<User> users;
}
