package com.staynest.repository;

import com.staynest.entity.User;
import com.staynest.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    //find user by email(For Login)
    Optional<User> findByEmail(String email);

    //Check if email already exist (For registration)
    boolean existsByEmail(String email);

    //find all users by role
    List<User> findByRole(UserRole role);

    //Find all verified User
    List<User> findByIsVerified(Boolean isVerified);

    //find by user by role and verification status
    List<User> findByRoleAndIsVerified(UserRole role, Boolean isVerified);


}
