package com.clbs.investment.domain.enums;

/**
 * Maps to COBOL POSMSTRE WS-ACCOUNT-STATUS values.
 * A = Active, I = Inactive, C = Closed
 */
public enum AccountStatus {
    A("Active"),
    I("Inactive"),
    C("Closed");

    private final String description;

    AccountStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
