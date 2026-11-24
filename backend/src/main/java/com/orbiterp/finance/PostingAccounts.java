package com.orbiterp.finance;

import java.util.List;

/** The default chart of accounts, and the accounts that automatic postings use. */
final class PostingAccounts {

    static final String CASH = "1000";
    static final String RECEIVABLES = "1100";
    static final String SALES_TAX = "2100";
    static final String SALES_REVENUE = "4000";

    static final List<DefaultAccount> DEFAULT_CHART = List.of(
            new DefaultAccount(CASH, "Cash and bank", AccountType.ASSET),
            new DefaultAccount(RECEIVABLES, "Accounts receivable", AccountType.ASSET),
            new DefaultAccount("1200", "Inventory", AccountType.ASSET),
            new DefaultAccount("2000", "Accounts payable", AccountType.LIABILITY),
            new DefaultAccount(SALES_TAX, "Sales tax payable", AccountType.LIABILITY),
            new DefaultAccount("3000", "Owner's equity", AccountType.EQUITY),
            new DefaultAccount(SALES_REVENUE, "Sales revenue", AccountType.REVENUE),
            new DefaultAccount("5000", "Cost of goods sold", AccountType.EXPENSE),
            new DefaultAccount("6000", "Operating expenses", AccountType.EXPENSE));

    private PostingAccounts() {
    }

    record DefaultAccount(String code, String name, AccountType type) {
    }
}
