package com.bookmyshow.auth.repository;

import com.bookmyshow.auth.entity.Role;
import com.bookmyshow.auth.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByName(RoleName name);
}
