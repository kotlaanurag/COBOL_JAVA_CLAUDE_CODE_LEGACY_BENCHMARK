package com.clbs.investment.domain.enums;

/**
 * Maps to COBOL TRANFILE WS-TRAN-TYPE values.
 * BY = Buy, SL = Sell, FE = Fee
 */
public enum TransactionType {
    BY("Buy"),
    SL("Sell"),
    FE("Fee");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
