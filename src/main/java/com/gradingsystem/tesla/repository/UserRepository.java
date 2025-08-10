package com.gradingsystem.tesla.repository;

import com.gradingsystem.tesla.model.Institution;
import com.gradingsystem.tesla.model.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByInstitutionAndRole(Institution institution, String role);

    List<User> findByInstitutionAndRoleAndApprovedByAdminFalse(Institution institution, String role);

    User findByRegistrationId(String registrationId);

    User findByRegistrationIdAndInstitution(String registrationId, Institution institution);

    User findByIdAndInstitution(Long id, Institution institution);

}
