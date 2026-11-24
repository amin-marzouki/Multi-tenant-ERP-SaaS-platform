package com.orbiterp.finance;

import com.orbiterp.common.TenantScopedRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface JournalLineRepository extends TenantScopedRepository<JournalLine, Long> {

    @Query("select l.account.id as accountId, sum(l.debit) as debit, sum(l.credit) as credit "
            + "from JournalLine l group by l.account.id")
    List<AccountTotals> totalsByAccount();

    interface AccountTotals {
        Long getAccountId();

        BigDecimal getDebit();

        BigDecimal getCredit();
    }
}
