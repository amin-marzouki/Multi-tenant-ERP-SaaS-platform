package com.orbiterp.auth;

import com.orbiterp.common.TenantScopedRepository;

import java.util.Collection;
import java.util.List;

public interface RoleRepository extends TenantScopedRepository<Role, Long> {

    List<Role> findByNameIn(Collection<String> names);
}
