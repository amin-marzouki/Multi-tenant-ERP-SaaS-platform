package com.orbiterp.finance;

import com.orbiterp.common.BusinessException;
import com.orbiterp.common.Money;
import com.orbiterp.common.NotFoundException;
import com.orbiterp.common.sequence.DocumentNumberService;
import com.orbiterp.finance.FinanceDtos.JournalEntryRequest;
import com.orbiterp.finance.FinanceDtos.JournalEntryResponse;
import com.orbiterp.finance.FinanceDtos.JournalEntrySummary;
import com.orbiterp.finance.FinanceDtos.JournalLineRequest;
import com.orbiterp.finance.FinanceDtos.JournalLineResponse;
import com.orbiterp.finance.FinanceDtos.TrialBalance;
import com.orbiterp.finance.FinanceDtos.TrialBalanceRow;
import com.orbiterp.finance.JournalLineRepository.AccountTotals;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JournalService {

    private final JournalEntryRepository entries;
    private final JournalLineRepository lines;
    private final AccountRepository accountRepository;
    private final AccountService accounts;
    private final DocumentNumberService numbers;

    /** One line of an entry about to be posted: either a debit or a credit. */
    public record DraftLine(Account account, String description, BigDecimal debit, BigDecimal credit) {
    }

    /** Posts a balanced entry. Every line must have exactly one of debit or credit above zero. */
    @Transactional
    public JournalEntry post(LocalDate date, String description, JournalSource source, Long sourceId,
                             String sourceReference, List<DraftLine> draftLines) {
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        JournalEntry entry = new JournalEntry();
        for (DraftLine draft : draftLines) {
            BigDecimal debit = Money.round(Money.orZero(draft.debit()));
            BigDecimal credit = Money.round(Money.orZero(draft.credit()));
            boolean hasDebit = debit.signum() > 0;
            boolean hasCredit = credit.signum() > 0;
            if (debit.signum() < 0 || credit.signum() < 0 || hasDebit == hasCredit) {
                throw BusinessException.invalid("INVALID_JOURNAL_LINE",
                        "Each line needs either a debit or a credit above zero, not both");
            }
            JournalLine line = new JournalLine();
            line.setAccount(draft.account());
            line.setDescription(draft.description() == null || draft.description().isBlank() ? null : draft.description().trim());
            line.setDebit(debit);
            line.setCredit(credit);
            entry.addLine(line);
            totalDebit = totalDebit.add(debit);
            totalCredit = totalCredit.add(credit);
        }
        if (entry.getLines().size() < 2 || totalDebit.compareTo(totalCredit) != 0) {
            throw BusinessException.invalid("UNBALANCED_ENTRY", "Debits (%s) must equal credits (%s)"
                    .formatted(totalDebit.toPlainString(), totalCredit.toPlainString()));
        }
        entry.setNumber(numbers.next("JOURNAL", "JE"));
        entry.setEntryDate(date != null ? date : LocalDate.now());
        entry.setDescription(description);
        entry.setSourceType(source);
        entry.setSourceId(sourceId);
        entry.setSourceReference(sourceReference);
        entry.setAmount(totalDebit);
        return entries.save(entry);
    }

    @Transactional
    public JournalEntryResponse createManual(JournalEntryRequest request) {
        List<DraftLine> draftLines = new ArrayList<>();
        for (JournalLineRequest line : request.lines()) {
            Account account = accounts.require(line.accountId());
            if (!account.isActive()) {
                throw BusinessException.invalid("INACTIVE_ACCOUNT", "Account " + account.getCode() + " is not active");
            }
            draftLines.add(new DraftLine(account, line.description(), line.debit(), line.credit()));
        }
        JournalEntry entry = post(request.entryDate(), request.description().trim(), JournalSource.MANUAL,
                null, null, draftLines);
        return toResponse(entry);
    }

    @Transactional(readOnly = true)
    public List<JournalEntrySummary> list() {
        return entries.findTop200ByOrderByIdDesc().stream()
                .map(e -> new JournalEntrySummary(e.getId(), e.getNumber(), e.getEntryDate(), e.getDescription(),
                        e.getSourceType(), e.getSourceReference(), e.getAmount()))
                .toList();
    }

    @Transactional(readOnly = true)
    public JournalEntryResponse get(Long id) {
        return toResponse(entries.findWithLinesById(id).orElseThrow(() -> new NotFoundException("Journal entry", id)));
    }

    /** Debit and credit totals of every account that has postings. */
    @Transactional(readOnly = true)
    public TrialBalance trialBalance() {
        Map<Long, AccountTotals> totals = lines.totalsByAccount().stream()
                .collect(Collectors.toMap(AccountTotals::getAccountId, Function.identity()));
        List<TrialBalanceRow> rows = new ArrayList<>();
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        for (Account account : accountRepository.findAllByOrderByCodeAsc()) {
            AccountTotals t = totals.get(account.getId());
            if (t == null) {
                continue;
            }
            BigDecimal debit = Money.orZero(t.getDebit());
            BigDecimal credit = Money.orZero(t.getCredit());
            rows.add(new TrialBalanceRow(account.getId(), account.getCode(), account.getName(), account.getType(),
                    debit, credit, debit.subtract(credit)));
            totalDebit = totalDebit.add(debit);
            totalCredit = totalCredit.add(credit);
        }
        return new TrialBalance(rows, totalDebit, totalCredit);
    }

    private static JournalEntryResponse toResponse(JournalEntry e) {
        List<JournalLineResponse> lineResponses = e.getLines().stream()
                .map(l -> new JournalLineResponse(l.getId(), l.getLineNo(), l.getAccount().getId(),
                        l.getAccount().getCode(), l.getAccount().getName(), l.getDescription(), l.getDebit(), l.getCredit()))
                .toList();
        return new JournalEntryResponse(e.getId(), e.getNumber(), e.getEntryDate(), e.getDescription(),
                e.getSourceType(), e.getSourceReference(), e.getAmount(), lineResponses);
    }
}
