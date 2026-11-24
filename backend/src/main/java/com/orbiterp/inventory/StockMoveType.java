package com.orbiterp.inventory;

public enum StockMoveType {
    /** Goods came in. */
    RECEIPT,
    /** Goods left for a shipped sales order. */
    ISSUE,
    /** A count correction, up or down. */
    ADJUSTMENT
}
