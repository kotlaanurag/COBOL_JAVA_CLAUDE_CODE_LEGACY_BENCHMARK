package com.clbs.investment.exception;

/**
 * Thrown by PositionUpdateService (POSUPD00) when a position update fails.
 * Typically wraps optimistic lock failures (concurrent updates) or constraint violations.
 */
public class PositionUpdateException extends RuntimeException {

    private final Long accountNo;
    private final String fundId;

    public PositionUpdateException(Long accountNo, String fundId, String message) {
        super(message);
        this.accountNo = accountNo;
        this.fundId = fundId;
    }

    public PositionUpdateException(Long accountNo, String fundId, String message, Throwable cause) {
        super(message, cause);
        this.accountNo = accountNo;
        this.fundId = fundId;
    }

    public Long getAccountNo() {
        return accountNo;
    }

    public String getFundId() {
        return fundId;
    }
}
