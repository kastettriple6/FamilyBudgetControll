package com.example.familyBudgetControll.controller;

import com.example.familyBudgetControll.dto.FamilyDTO;
import com.example.familyBudgetControll.dto.UserDTO;
import com.example.familyBudgetControll.dto.WithdrawLimitDTO;
import com.example.familyBudgetControll.entity.Family;
import com.example.familyBudgetControll.entity.Users;
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

    @PostMapping("/registration")
    @ResponseBody
    public Optional<Users> registration(UserDTO userDTO) {
        return service.registration(userDTO);
    }

    @PutMapping("/user/family{familyId}")
    public List<Users> addUserToFamily(Long userId, @PathVariable Long familyId) {
        return service.putUserToFamilyList(userId, familyId);
    }

    @GetMapping("/user/families")
    public List<Family> displayFamilies() {
        return service.showAllFamilies();
    }

    @PostMapping("/user/family/create")
    @ResponseBody
    public Family createFamily(@RequestBody FamilyDTO familyDTO) {
        return service.createFamily(familyDTO);
    }

    @GetMapping("/user/family{familyId}")
    @ResponseBody
    public List<Users> displayFamilyList(@PathVariable Long familyId) {
        return service.checkFamilyList(familyId);
    }

    @GetMapping("/user/family{familyId}_balance")
    public Double checkFamilyBalance(@PathVariable Long familyId) {
        return service.checkFamilyBalance(familyId);
    }

    @PutMapping("/user/family{familyId}_balance/refill")
    public Double refillFamilyBalance(Double sumToRefill, @PathVariable Long familyId) {
        return service.refillFamilyBalance(sumToRefill, familyId);
    }

    @PutMapping("/user{userId}_balance/withdraw")
    public Double withdrawFromFamilyBalance(@PathVariable Long userId, Double sumToWithdraw) {
        return service.withdraw(userId, sumToWithdraw);
    }

    @PreAuthorize("hasRole('GLOBAL_ADMIN')")
    @PutMapping("/ga/family{familyId}_balance/set_limit")
    @ResponseBody
    public WithdrawLimit setLimitForFamily(@PathVariable Long familyId, WithdrawLimitDTO limit) {
        return service.setWithdrawLimitForFamily(familyId, limit);
    }

    @PreAuthorize("hasRole('GLOBAL_ADMIN')")
    @PutMapping("/ga/user{userId}/set_limit")
    @ResponseBody
    public WithdrawLimit setLimitForUser(@PathVariable Long userId, WithdrawLimitDTO limit) {
        return service.setWithdrawLimitForUser(userId, limit);
    }

    @RolesAllowed({"GLOBAL_ADMIN", "FAMILY_ADMIN"})
    @PutMapping("/fa/family/set_limit/family/user{userId}")
    @ResponseBody
    public WithdrawLimit setLimitForFamilyByAdmin(Long adminId, @RequestBody WithdrawLimitDTO limit) {
        return service.setFamilyLimitByAdmin(adminId, limit);
    }

    @RolesAllowed({"GLOBAL_ADMIN", "FAMILY_ADMIN"})
    @PutMapping("/fa/family/set_limit/user/user{userToSetLimit}")
    @ResponseBody
    public WithdrawLimit setLimitForUserInFamilyByFamilyAdmin(@PathVariable Long userToSetLimit, @RequestBody WithdrawLimitDTO limit) {
        return service.setLimitForUserByAdmin(userToSetLimit, limit);
    }
}
