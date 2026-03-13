package com.clbs.investment.domain.enums;

/**
 * Maps to COBOL BCHCTL WS-PROCESS-STATUS values.
 * W = Waiting, P = In-Process, C = Complete, E = Error
 */
public enum BatchStatus {
    W("Waiting"),
    P("In-Process"),
    C("Complete"),
    E("Error");

    private final String description;

    BatchStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
