package com.orbiterp.finance;

import com.orbiterp.common.BusinessException;
import com.orbiterp.common.NotFoundException;
import com.orbiterp.finance.FinanceDtos.AccountRequest;
import com.orbiterp.finance.FinanceDtos.AccountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accounts;

    @Transactional(readOnly = true)
    public List<AccountResponse> list() {
        return accounts.findAllByOrderByCodeAsc().stream().map(AccountService::toResponse).toList();
    }

    @Transactional
    public AccountResponse create(AccountRequest request) {
        String code = request.code().trim();
        if (accounts.existsByCode(code)) {
            throw BusinessException.conflict("ACCOUNT_CODE_TAKEN", "Account " + code + " already exists");
        }
        return toResponse(accounts.save(newAccount(code, request.name().trim(), request.type())));
    }

    /** Every new tenant starts with a small default chart of accounts. */
    @Transactional
    public void createDefaultChart() {
        for (PostingAccounts.DefaultAccount account : PostingAccounts.DEFAULT_CHART) {
            if (!accounts.existsByCode(account.code())) {
                accounts.save(newAccount(account.code(), account.name(), account.type()));
            }
        }
    }

    Account require(Long id) {
        return accounts.findById(id).orElseThrow(() -> new NotFoundException("Account", id));
    }

    /** The account an automatic posting needs; fails clearly if someone removed it. */
    Account requireByCode(String code) {
        return accounts.findByCode(code).orElseThrow(() -> BusinessException.conflict("MISSING_ACCOUNT",
                "Account " + code + " is needed for automatic postings but does not exist"));
    }

    private static Account newAccount(String code, String name, AccountType type) {
        Account account = new Account();
        account.setCode(code);
        account.setName(name);
        account.setType(type);
        return account;
    }

    static AccountResponse toResponse(Account a) {
        return new AccountResponse(a.getId(), a.getCode(), a.getName(), a.getType(), a.isActive());
    }
}
