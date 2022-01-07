package com.example.familyBudgetControll.controller;

import com.example.familyBudgetControll.dto.FamilyDTO;
import com.example.familyBudgetControll.dto.UserDTO;
import com.example.familyBudgetControll.dto.WithdrawLimitDTO;
import com.example.familyBudgetControll.entity.Family;
import com.example.familyBudgetControll.entity.User;
import com.example.familyBudgetControll.entity.WithdrawLimit;
import com.example.familyBudgetControll.service.FamilyBudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/fbc")
public class FamilyBudgetController {

    @Autowired
    private FamilyBudgetService service;

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        String errorMessge = null;
        if(error != null) {
            errorMessge = "Username or Password is incorrect !!";
        }
        if(logout != null) {
            errorMessge = "You have been successfully logged out !!";
        }
        model.addAttribute("errorMessage", errorMessge);
        return "login";
    }

    @GetMapping("/logout")
    public String logoutPage (HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null){
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/login?logout=true";
    }
    @PostMapping("/registration")
    @ResponseBody
    public User registration(UserDTO userDTO) {
        return service.registration(userDTO);
    }

    @PutMapping("/family{familyId}")
    public List<User> addUserToFamily(Long userId, @PathVariable Long familyId) {
        return service.putUserToFamilyList(userId, familyId);
    }

    @GetMapping("/families")
    public List<Family> displayFamilies(){
        return service.showAllFamilies();
    }

    @PostMapping("/family/create")
    @ResponseBody
    public Family createFamily(@RequestBody FamilyDTO familyDTO) {
        return service.createFamily(familyDTO);
    }

    @GetMapping("/family{familyId}")
    @ResponseBody
    public List<User> displayFamilyList(@PathVariable Long familyId) {
        return service.checkFamilyList(familyId);
    }

    @GetMapping("/family{familyId}_balance")
    public Double checkFamilyBalance(@PathVariable Long familyId) {
        return service.checkFamilyBalance(familyId);
    }

    @PutMapping("/family{familyId}_balance/refill")
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
    public WithdrawLimit setLimitForFamily(@PathVariable Long adminId, @PathVariable Long familyId, WithdrawLimitDTO limit) {
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
    public WithdrawLimit setLimitForUserInFamilyByFamilyAdmin(@PathVariable Long userToSetLimit, Long adminId, @RequestBody WithdrawLimitDTO limit) {
        return service.setLimitForUserByAdmin(userToSetLimit, adminId, limit);
    }
}
