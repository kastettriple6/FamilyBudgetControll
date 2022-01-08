package com.example.familyBudgetControll.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.persistence.*;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userName;
    private String password;
    @Transient
    private String passwordConfirm;
    @Column
    private String firstName;
    @Column
    private String lastName;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "limit_id")
    private WithdrawLimit limit;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "family_id")
    private Family family;

    @ManyToMany
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(
                    name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "role_id", referencedColumnName = "id"))
    private Collection<Role> roles;

    public Users(String userName, String password, List<SimpleGrantedAuthority> authorities) {
    }

    public Users() {
    }

    public boolean isActive() {
        return true;
    }
}
