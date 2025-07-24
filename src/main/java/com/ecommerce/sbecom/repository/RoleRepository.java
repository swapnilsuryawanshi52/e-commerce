package com.ecommerce.sbecom.repository;

import com.ecommerce.sbecom.entity.AppRole;
import com.ecommerce.sbecom.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(AppRole appRole);
}
