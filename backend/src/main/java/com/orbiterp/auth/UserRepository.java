package com.orbiterp.auth;

import com.orbiterp.common.TenantScopedRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends TenantScopedRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findAllByOrderByUsernameAsc();
}
