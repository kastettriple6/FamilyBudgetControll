package com.example.familyBudgetControll.service;

import com.example.familyBudgetControll.dto.FamilyDTO;
import com.example.familyBudgetControll.dto.UserDTO;
import com.example.familyBudgetControll.dto.WithdrawLimitDTO;
import com.example.familyBudgetControll.entity.Family;
import com.example.familyBudgetControll.entity.Role;
import com.example.familyBudgetControll.entity.Users;
import com.example.familyBudgetControll.entity.WithdrawLimit;
import com.example.familyBudgetControll.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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
    @Autowired
    private PasswordEncoder encoder;

    public Optional<Users> registration(UserDTO userDTO) {
        Users userEntity = new Users();
        userEntity.setId(userDTO.getId());
        userEntity.setUserName(userDTO.getUserName());
        userEntity.setPassword(encoder.encode(userDTO.getPassword()));

        WithdrawLimit limit = new WithdrawLimit();

        limitRepository.save(limit);

        userEntity.setLimit(limit);

        roleRepository.save(new Role("USER"));

        userEntity.setRoles(Collections.singletonList(roleRepository.findByName("USER")));

        userRepository.save(userEntity);

        return userRepository.findByUserName(userEntity.getUserName());
    }

    public Family createFamily(FamilyDTO familyDTO) {
        Family familyEntity = new Family();
        familyEntity.setName(familyDTO.getName());
        familyEntity.setBalance(0d);
        familyEntity.setSumOfWithdrawsByDay(0d);

        WithdrawLimit limit = new WithdrawLimit();

        limitRepository.save(limit);

        familyEntity.setLimit(limit);
        return familyRepository.save(familyEntity);
    }

    public List<Users> putUserToFamilyList(Long userId, Long familyId) {
        Users userEntity = userRepository.findById(userId).orElseThrow(IllegalArgumentException::new);
        Family familyEntity = familyRepository.findById(familyId).orElseThrow(IllegalArgumentException::new);
        userEntity.setFamily(familyEntity);
        userRepository.save(userEntity);
        return familyEntity.getUsers();
    }

    public List<Users> checkFamilyList(Long familyId) {
        Family familyEntity = familyRepository.findById(familyId).orElseThrow(IllegalArgumentException::new);
        Comparator<Users> userComparator = (o1, o2) -> {
            int fComp = o1.getUserName().compareTo(o2.getUserName());

            if (fComp != 0) {
                return fComp;
            }

            Integer r1 = o1.getRoles().size();
            Integer r2 = o2.getRoles().size();
            return r1.compareTo(r2);
        };
        List<Users> familyMembers = familyEntity.getUsers();
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
        Users userEntity = userRepository.findById(userId).orElseThrow(IllegalArgumentException::new);
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
        if(limit.getDateForLimit() == null) {
            limitForFamily.setDateForLimit(LocalDate.of(1970, 1, 1));
        } else {
            limitForFamily.setDateForLimit(limit.getDateForLimit());
        }
        limitForFamily.setLimitForSingleWithdraw(limit.getLimitForSingleWithdraw());
        limitForFamily.setLimitPerDay(limit.getLimitPerDay());
        limitForFamily.setLimitByDate(limit.getLimitByDate());
        limitForFamily.setFamily(familyRepository.getById(familyId));

        limitRepository.save(limitForFamily);

        familyEntity.setLimit(limitForFamily);
        familyRepository.save(familyEntity);
        return familyEntity.getLimit();
    }

    public WithdrawLimit setWithdrawLimitForUser(Long userId, WithdrawLimitDTO limit) {
        Users userEntity = userRepository.findById(userId).orElseThrow(IllegalArgumentException::new);
        WithdrawLimit limitForUser = new WithdrawLimit();
        if(limit.getDateForLimit() == null) {
            limitForUser.setDateForLimit(LocalDate.of(1970, 1, 1));
        } else {
            limitForUser.setDateForLimit(limit.getDateForLimit());
        }
        limitForUser.setLimitForSingleWithdraw(limit.getLimitForSingleWithdraw());
        limitForUser.setLimitPerDay(limit.getLimitPerDay());
        limitForUser.setLimitByDate(limit.getLimitByDate());
        limitForUser.setUser(userRepository.getById(userId));

        limitRepository.save(limitForUser);

        userEntity.setLimit(limitForUser);

        userRepository.save(userEntity);
        return userEntity.getLimit();
    }

    public WithdrawLimit setFamilyLimitByAdmin(Long familyId, WithdrawLimitDTO limit) {
        Family neededFamily = familyRepository.findById(familyId).orElseThrow(IllegalArgumentException::new);
        WithdrawLimit limitForFamily = new WithdrawLimit();
        if(limit.getDateForLimit() == null) {
            limitForFamily.setDateForLimit(LocalDate.of(1970, 1, 1));
        } else {
            limitForFamily.setDateForLimit(limit.getDateForLimit());
        }
        limitForFamily.setLimitForSingleWithdraw(limit.getLimitForSingleWithdraw());
        limitForFamily.setLimitPerDay(limit.getLimitPerDay());
        limitForFamily.setLimitByDate(limit.getLimitByDate());
        limitForFamily.setFamily(familyRepository.getById(familyId));

        limitRepository.save(limitForFamily);

        neededFamily.setLimit(limitForFamily);

        familyRepository.save(neededFamily);

        return neededFamily.getLimit();
    }

    public WithdrawLimit setLimitForUserByAdmin(Long userId, WithdrawLimitDTO limit) {
        Users userToSetLimit = userRepository.findById(userId).orElseThrow(IllegalArgumentException::new);
        WithdrawLimit limitForUser = new WithdrawLimit();
        if(limit.getDateForLimit() == null) {
            limitForUser.setDateForLimit(LocalDate.of(1970, 1, 1));
        } else {
            limitForUser.setDateForLimit(limit.getDateForLimit());
        }
        limitForUser.setLimitForSingleWithdraw(limit.getLimitForSingleWithdraw());
        limitForUser.setLimitPerDay(limit.getLimitPerDay());
        limitForUser.setLimitByDate(limit.getLimitByDate());
        limitForUser.setUser(userRepository.getById(userId));

        limitRepository.save(limitForUser);

        userToSetLimit.setLimit(limitForUser);

        userRepository.save(userToSetLimit);

        return userToSetLimit.getLimit();
    }

    public List<Family> showAllFamilies() {
        return familyRepository.findAll();
    }
}
