package com.clbs.investment.domain.enums;

/**
 * Maps to COBOL batch return codes (RTNCDE00).
 * 0000 = Success, 0004 = Warning, 0008 = Errors, 0012 = Critical, 0016 = Environment error
 */
public enum ReturnCode {
    SUCCESS(0, "Success - all records processed normally"),
    WARNING(4, "Warning - some records processed with warnings"),
    ERROR(8, "Error - some records rejected"),
    CRITICAL(12, "Critical - processing halted, manual intervention required"),
    ENVIRONMENT(16, "Environment error - system resource unavailable");

    private final int code;
    private final String description;

    ReturnCode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ReturnCode fromCode(int code) {
        for (ReturnCode rc : values()) {
            if (rc.code == code) return rc;
        }
        throw new IllegalArgumentException("Unknown return code: " + code);
    }
}
