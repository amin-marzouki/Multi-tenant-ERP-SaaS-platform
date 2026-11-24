package com.orbiterp.finance;

import com.orbiterp.common.TenantScopedRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface JournalEntryRepository extends TenantScopedRepository<JournalEntry, Long> {

    List<JournalEntry> findTop200ByOrderByIdDesc();

    @EntityGraph(attributePaths = {"lines", "lines.account"})
    Optional<JournalEntry> findWithLinesById(Long id);
}
