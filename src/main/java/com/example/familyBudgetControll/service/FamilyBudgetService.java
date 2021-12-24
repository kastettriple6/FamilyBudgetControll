package com.example.familyBudgetControll.service;

import com.example.familyBudgetControll.dto.FamilyDTO;
import com.example.familyBudgetControll.dto.UserDTO;
import com.example.familyBudgetControll.entity.Family;
import com.example.familyBudgetControll.entity.Role;
import com.example.familyBudgetControll.entity.User;
import com.example.familyBudgetControll.entity.WithdrawLimit;
import com.example.familyBudgetControll.repository.FamilyRepository;
import com.example.familyBudgetControll.repository.PrivilegeRepository;
import com.example.familyBudgetControll.repository.RoleRepository;
import com.example.familyBudgetControll.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collection;
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

    public Optional<User> login(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(UserDTO userDTO) {
        User userEntity = new User();
        userEntity.setFirstName(userDTO.getFirstName());
        userEntity.setLastName(userDTO.getLastName());
        return userRepository.save(userEntity);
    }

    public List<User> putUserToFamilyList(Long userId, Long familyId) {
        User userEntity = userRepository.findById(userId).orElseThrow(IllegalArgumentException::new);
        Family familyEntity = familyRepository.findById(familyId).orElseThrow(IllegalArgumentException::new);
        List<User> usersInFamily = familyEntity.getUsers();
        usersInFamily.add(userEntity);
        familyEntity.setUsers(usersInFamily);
        familyRepository.save(familyEntity);
        return usersInFamily;
    }

    public Family createFamily(FamilyDTO familyDTO) {
        Family familyEntity = new Family();
        familyEntity.setName(familyDTO.getName());
        return familyRepository.save(familyEntity);
    }

    public List<User> checkFamilyList(Long familyId) {
        Family familyEntity = familyRepository.findById(familyId).orElseThrow(IllegalArgumentException::new);
        Comparator<User> userComparator = new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                int fComp = o1.getFirstName().compareTo(o2.getFirstName());

                if(fComp != 0) {
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

    public Float checkFamilyBalance(Long familyId) {
        Family familyEntity = familyRepository.findById(familyId).orElseThrow(IllegalArgumentException::new);
        return familyEntity.getBalance();
    }

    @Transactional
    public Float refillFamilyBalance(Float sumToRefill, Long familyId) {
        Family familyEntity = familyRepository.findById(familyId).orElseThrow(IllegalArgumentException::new);
        familyEntity.setBalance(familyEntity.getBalance() + sumToRefill);
        familyRepository.save(familyEntity);
        return familyEntity.getBalance();
    }

    @Transactional
    public Float withdraw(Long userId, Float sumToWithdraw) {
        User userEntity = userRepository.findById(userId).orElseThrow(IllegalArgumentException::new);
        if (userEntity.getLimit().getLimitForSingleWithdraw() < sumToWithdraw) {
            logger.error("limit exceeded");
            if (userEntity.getLimit().getLimitPerDay() < sumToWithdraw) {
                logger.error("limit exceeded");
                if (userEntity.getLimit().getLimitByDate() < sumToWithdraw) {
                    logger.error("limit exceeded");
                }
            }
        } else {
            Family familyEntity = userEntity.getFamily();
            familyEntity.setBalance(familyEntity.getBalance() - sumToWithdraw);
            familyRepository.save(familyEntity);
            return familyEntity.getBalance();
        }
        return userEntity.getFamily().getBalance();
    }

    public WithdrawLimit setWithdrawLimitForFamily(Long familyId, WithdrawLimit limit) {
        Family familyEntity = familyRepository.findById(familyId).orElseThrow(IllegalArgumentException::new);
        familyEntity.setLimit(limit);
        familyRepository.save(familyEntity);
        return familyEntity.getLimit();
    }

    public WithdrawLimit setWithdrawLimitForUser(Long userId, WithdrawLimit limit) {
        User userEntity = userRepository.findById(userId).orElseThrow(IllegalArgumentException::new);
        userEntity.setLimit(limit);
        userRepository.save(userEntity);
        return userEntity.getLimit();
    }

    public WithdrawLimit setFamilyLimitByAdmin(Long userId, WithdrawLimit limit) {
        User familyAdmin = userRepository.findById(userId).orElseThrow(IllegalArgumentException::new);
        Role neededRole = roleRepository.findByName("FAMILY_ADMIN");
        Family neededFamily = familyRepository.findById(familyAdmin.getFamily().getId()).orElseThrow(IllegalArgumentException::new);
        if (familyAdmin.getRoles().contains(neededRole)) {
            if (familyAdmin.getFamily().equals(neededFamily)) {
                neededFamily.setLimit(limit);
                familyRepository.save(neededFamily);
            }
        } else {
            logger.error("access_denied");
        }
        return neededFamily.getLimit();
    }

    public WithdrawLimit setLimitForUserByAdmin(Long userId, Long adminId, WithdrawLimit limit) {
        User admin = userRepository.findById(adminId).orElseThrow(IllegalArgumentException::new);
        User userToSetLimit = userRepository.findById(userId).orElseThrow(IllegalArgumentException::new);
        Role neededRoleForAdmin = roleRepository.findByName("FAMILY_ADMIN");
        if (admin.getRoles().contains(neededRoleForAdmin)) {
            if (admin.getFamily().equals(userToSetLimit.getFamily())) {
                userToSetLimit.setLimit(limit);
                userRepository.save(userToSetLimit);
            } else {
                logger.error("user associated with other family");
            }
        } else {
            logger.error("access denied");
        }
        return userToSetLimit.getLimit();
    }
}
