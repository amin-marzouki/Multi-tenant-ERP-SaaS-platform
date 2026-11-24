package com.orbiterp.finance;

import com.orbiterp.finance.FinanceDtos.JournalEntryRequest;
import com.orbiterp.finance.FinanceDtos.JournalEntryResponse;
import com.orbiterp.finance.FinanceDtos.JournalEntrySummary;
import com.orbiterp.finance.FinanceDtos.TrialBalance;
import com.orbiterp.security.Access;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/finance")
@PreAuthorize(Access.FINANCE)
@RequiredArgsConstructor
public class JournalController {

    private final JournalService journalService;

    @GetMapping("/journal-entries")
    public List<JournalEntrySummary> list() {
        return journalService.list();
    }

    @GetMapping("/journal-entries/{id}")
    public JournalEntryResponse get(@PathVariable Long id) {
        return journalService.get(id);
    }

    @PostMapping("/journal-entries")
    @ResponseStatus(HttpStatus.CREATED)
    public JournalEntryResponse create(@Valid @RequestBody JournalEntryRequest request) {
        return journalService.createManual(request);
    }

    @GetMapping("/trial-balance")
    public TrialBalance trialBalance() {
        return journalService.trialBalance();
    }
}
