package com.example.familyBudgetControll.service;

import com.example.familyBudgetControll.dto.FamilyDTO;
import com.example.familyBudgetControll.dto.UserDTO;
import com.example.familyBudgetControll.dto.WithdrawLimitDTO;
import com.example.familyBudgetControll.entity.Family;
import com.example.familyBudgetControll.entity.Role;
import com.example.familyBudgetControll.entity.User;
import com.example.familyBudgetControll.entity.WithdrawLimit;
import com.example.familyBudgetControll.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class FamilyBudgetService {
    private final Logger logger = LoggerFactory.getLogger(FamilyBudgetService.class);
    @Autowired
    private FamilyRepository familyRepository;
    @Autowired
    private PrivilegeRepository privilegeRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WithdrawLimitRepository limitRepository;

    public User registration(UserDTO userDTO){
        User userEntity = new User();
        userEntity.setUserName(userDTO.getUserName());
        userEntity.setPassword(userDTO.getPassword());
        userEntity.setPasswordConfirm(userDTO.getPasswordConfirm());
        userEntity.setFirstName(userDTO.getFirstName());
        userEntity.setLastName(userDTO.getLastName());
        if(userEntity.getPasswordConfirm().equals(userDTO.getPasswordConfirm())){
            userRepository.save(userEntity);
        } else{
            logger.error("password confirmation failed");
        }
        return userEntity;
    }

    public List<User> putUserToFamilyList(Long userId, Long familyId) {
        User userEntity = userRepository.findById(userId).orElseThrow(IllegalArgumentException::new);
        Family familyEntity = familyRepository.findById(familyId).orElseThrow(IllegalArgumentException::new);
        userEntity.setFamily(familyEntity);
        userRepository.save(userEntity);
        return familyEntity.getUsers();
    }

    public Family createFamily(FamilyDTO familyDTO) {
        Family familyEntity = new Family();
        familyEntity.setName(familyDTO.getName());
        familyEntity.setBalance(0d);
        familyEntity.setSumOfWithdrawsByDay(0d);
        return familyRepository.save(familyEntity);
    }

    public List<User> checkFamilyList(Long familyId) {
        Family familyEntity = familyRepository.findById(familyId).orElseThrow(IllegalArgumentException::new);
        Comparator<User> userComparator = new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                int fComp = o1.getFirstName().compareTo(o2.getFirstName());

                if (fComp != 0) {
                    return fComp;
                }

                Integer r1 = o1.getRoles().size();
                Integer r2 = o2.getRoles().size();
                return r1.compareTo(r2);
            }
        };
        List<User> familyMembers = familyEntity.getUsers();
        familyMembers.sort(userComparator);
        return familyMembers;
    }

    public Double checkFamilyBalance(Long familyId) {
        Family familyEntity = familyRepository.findById(familyId).orElseThrow(IllegalArgumentException::new);
        return familyEntity.getBalance();
    }

    @Transactional
    public Double refillFamilyBalance(Double sumToRefill, Long familyId) {
        Family familyEntity = familyRepository.findById(familyId).orElseThrow(IllegalArgumentException::new);
        familyEntity.setBalance(familyEntity.getBalance() + sumToRefill);
        familyRepository.save(familyEntity);
        return familyEntity.getBalance();
    }

    @Transactional
    public Double withdraw(Long userId, Double sumToWithdraw) {
        User userEntity = userRepository.findById(userId).orElseThrow(IllegalArgumentException::new);
        Family familyEntity = userEntity.getFamily();
        if (userEntity.getLimit().getLimitForSingleWithdraw() < sumToWithdraw) {
            logger.error("limit exceeded");
            if (userEntity.getLimit().getLimitPerDay() < familyEntity.getSumOfWithdrawsByDay()) {
                logger.error("limit exceeded");
            }
        } else {
            if (userEntity.getLimit().getDateForLimit().equals(LocalDate.now())) {
                if (userEntity.getLimit().getLimitByDate() < familyEntity.getSumOfWithdrawsByDay()) {
                    logger.error("limit exceeded");
                }
            } else {
                familyEntity.setBalance(familyEntity.getBalance() - sumToWithdraw);
                familyEntity.setSumOfWithdrawsByDay(familyEntity.getSumOfWithdrawsByDay() + sumToWithdraw);
                familyRepository.save(familyEntity);
                return familyEntity.getBalance();
            }
        }
        return userEntity.getFamily().getBalance();
    }

    public WithdrawLimit setWithdrawLimitForFamily(Long familyId, WithdrawLimitDTO limit) {

        Family familyEntity = familyRepository.findById(familyId).orElseThrow(IllegalArgumentException::new);
        WithdrawLimit limitForFamily = new WithdrawLimit();
        limitForFamily.setDateForLimit(limit.getDateForLimit());
        limitForFamily.setLimitForSingleWithdraw(limit.getLimitForSingleWithdraw());
        limitForFamily.setLimitPerDay(limit.getLimitPerDay());
        limitForFamily.setLimitByDate(limit.getLimitByDate());
        familyEntity.setLimit(limitForFamily);
        familyRepository.save(familyEntity);
        return familyEntity.getLimit();
    }

    public WithdrawLimit setWithdrawLimitForUser(Long userId, WithdrawLimitDTO limit) {
        User userEntity = userRepository.findById(userId).orElseThrow(IllegalArgumentException::new);
        WithdrawLimit limitForUser = new WithdrawLimit();
        limitForUser.setDateForLimit(limit.getDateForLimit());
        limitForUser.setLimitForSingleWithdraw(limit.getLimitForSingleWithdraw());
        limitForUser.setLimitPerDay(limit.getLimitPerDay());
        limitForUser.setLimitByDate(limit.getLimitByDate());

        userEntity.setLimit(limitForUser);

        userRepository.save(userEntity);
        return userEntity.getLimit();
    }

    public WithdrawLimit setFamilyLimitByAdmin(Long userId, WithdrawLimitDTO limit) {
        User familyAdmin = userRepository.findById(userId).orElseThrow(IllegalArgumentException::new);
        Role neededRole = roleRepository.findByName("FAMILY_ADMIN");
        Family neededFamily = familyRepository.findById(familyAdmin.getFamily().getId()).orElseThrow(IllegalArgumentException::new);
        if (familyAdmin.getRoles().contains(neededRole)) {
            if (familyAdmin.getFamily().equals(neededFamily)) {
                WithdrawLimit limitForFamily = new WithdrawLimit();
                limitForFamily.setDateForLimit(limit.getDateForLimit());
                limitForFamily.setLimitForSingleWithdraw(limit.getLimitForSingleWithdraw());
                limitForFamily.setLimitPerDay(limit.getLimitPerDay());
                limitForFamily.setLimitByDate(limit.getLimitByDate());

                neededFamily.setLimit(limitForFamily);

                familyRepository.save(neededFamily);
            }
        } else {
            logger.error("access_denied");
        }
        return neededFamily.getLimit();
    }

    public WithdrawLimit setLimitForUserByAdmin(Long userId, Long adminId, WithdrawLimitDTO limit) {
        User admin = userRepository.findById(adminId).orElseThrow(IllegalArgumentException::new);
        User userToSetLimit = userRepository.findById(userId).orElseThrow(IllegalArgumentException::new);
        Role neededRoleForAdmin = roleRepository.findByName("FAMILY_ADMIN");
        if (admin.getRoles().contains(neededRoleForAdmin)) {
            if (admin.getFamily().equals(userToSetLimit.getFamily())) {
                WithdrawLimit limitForUser = new WithdrawLimit();
                limitForUser.setDateForLimit(limit.getDateForLimit());
                limitForUser.setLimitForSingleWithdraw(limit.getLimitForSingleWithdraw());
                limitForUser.setLimitPerDay(limit.getLimitPerDay());
                limitForUser.setLimitByDate(limit.getLimitByDate());

                userToSetLimit.setLimit(limitForUser);

                userRepository.save(userToSetLimit);
            } else {
                logger.error("user associated with other family");
            }
        } else {
            logger.error("access denied");
        }
        return userToSetLimit.getLimit();
    }

    public List<Family> showAllFamilies() {
        return familyRepository.findAll();
    }
}
