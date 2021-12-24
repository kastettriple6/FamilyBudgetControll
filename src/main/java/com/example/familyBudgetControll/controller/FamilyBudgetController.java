package com.example.familyBudgetControll.controller;

import com.example.familyBudgetControll.dto.FamilyDTO;
import com.example.familyBudgetControll.dto.UserDTO;
import com.example.familyBudgetControll.entity.Family;
import com.example.familyBudgetControll.entity.User;
import com.example.familyBudgetControll.entity.WithdrawLimit;
import com.example.familyBudgetControll.service.FamilyBudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import java.util.List;
import java.util.Optional;

@RestController
public class FamilyBudgetController {

    @Autowired
    private FamilyBudgetService service;

    @GetMapping("/login")
    public Optional<User> login(Long userId) {
        return service.login(userId);
    }

    @PostMapping("/registration")
    public User registerNewUser(@RequestBody UserDTO userDTO) {
        return service.createUser(userDTO);
    }

    @PutMapping("/family{familyId}")
    public List<User> addUserToFamily(Long userId, @PathVariable Long familyId) {
        return service.putUserToFamilyList(userId, familyId);
    }

    @PostMapping("/family/create")
    @ResponseBody
    public Family createFamily(@RequestBody FamilyDTO familyDTO) {
        return service.createFamily(familyDTO);
    }

    @GetMapping("/family{familyId}")
    public List<User> displayFamilyList(@PathVariable Long familyId) {
        return service.checkFamilyList(familyId);
    }

    @GetMapping("/family{familyId}_balance")
    public Float checkFamilyBalance(@PathVariable Long familyId) {
        return service.checkFamilyBalance(familyId);
    }

    @PutMapping("/family{familyId}_balance/refill")
    public Float refillFamilyBalance(Float sumToRefill, @PathVariable Long familyId) {
        return service.refillFamilyBalance(sumToRefill, familyId);
    }

    @PutMapping("/family{familyId}_balance/withdraw")
    public Float withdrawFromFamilyBalance(@PathVariable Long familyId, Float sumToWithdraw) {
        return service.withdraw(familyId, sumToWithdraw);
    }

    @PreAuthorize("hasRole('GLOBAL_ADMIN')")
    @PutMapping("/family{familyId}_balance/set_limit")
    @ResponseBody
    public WithdrawLimit setLimitForFamily(@PathVariable Long familyId, WithdrawLimit limit) {
        return service.setWithdrawLimitForFamily(familyId, limit);
    }

    @PreAuthorize("hasRole('GLOBAL_ADMIN')")
    @PutMapping("/user{userId}/set_limit")
    @ResponseBody
    public WithdrawLimit setLimitForUser(@PathVariable Long userId, WithdrawLimit limit) {
        return service.setWithdrawLimitForUser(userId, limit);
    }

    @RolesAllowed({"GLOBAL_ADMIN", "FAMILY_ADMIN"})
    @PutMapping("/family/user{userId}")
    @ResponseBody
    public WithdrawLimit setLimitForFamilyByAdmin (Long adminId, @RequestBody WithdrawLimit limit) {
        return service.setFamilyLimitByAdmin(adminId, limit);
    }

    @RolesAllowed({"GLOBAL_ADMIN", "FAMILY_ADMIN"})
    @PutMapping("/user{userToSetLimit}")
    @ResponseBody
    public WithdrawLimit setLimitForUserInFamilyByFamilyAdmin(@PathVariable Long userToSetLimit, Long adminId, @RequestBody WithdrawLimit limit) {
        return service.setLimitForUserByAdmin(userToSetLimit, adminId, limit);
    }
}
