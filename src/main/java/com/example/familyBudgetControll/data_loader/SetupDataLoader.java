package com.example.familyBudgetControll.data_loader;

import com.example.familyBudgetControll.entity.Privilege;
import com.example.familyBudgetControll.entity.Role;
import com.example.familyBudgetControll.repository.FamilyRepository;
import com.example.familyBudgetControll.repository.PrivilegeRepository;
import com.example.familyBudgetControll.repository.RoleRepository;
import com.example.familyBudgetControll.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.Collection;

@Component
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {
    private boolean alreadySetup = false;

    @Autowired
    private FamilyRepository familyRepository;

    @Autowired
    private PrivilegeRepository privilegeRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadySetup)
            return;

        Privilege readPrivilege = createPrivilegeIfNotFound("READ_PRIVILEGE");
        Privilege setLimitPrivilege = createPrivilegeIfNotFound("SET_LIMIT_PRIVILEGE");

        Role userRole = createRoleIfNotFound("USER_ROLE", Arrays.asList(readPrivilege));
        Role familyAdmin = createRoleIfNotFound("FAMILY_ADMIN", Arrays.asList(readPrivilege, setLimitPrivilege));
        Role globalAdmin = createRoleIfNotFound("GLOBAL_ADMIN", Arrays.asList(readPrivilege, setLimitPrivilege));


    }

    @Transactional
    Privilege createPrivilegeIfNotFound(String name) {

        Privilege privilege = privilegeRepository.findByName(name);
        if (privilege == null) {
            privilege = new Privilege(name);
            privilegeRepository.save(privilege);
        }
        return privilege;
    }

    @Transactional
    Role createRoleIfNotFound(String name, Collection<Privilege> privileges) {

        Role role = roleRepository.findByName(name);
        if (role == null) {
            role = new Role(name);
            role.setPrivileges(privileges);
            roleRepository.save(role);
        }
        return role;
    }
}
