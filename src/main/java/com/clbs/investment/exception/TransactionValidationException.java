package com.clbs.investment.exception;

/**
 * Thrown by TransactionValidationService (TRNVAL00) when a transaction fails validation.
 *
 * Maps to COBOL error codes:
 *  E001 = Invalid account or fund not found in POSMSTRE
 *  E002 = Future transaction date
 *  E003 = Sell would result in negative share balance
 *  E004 = Invalid transaction type
 */
public class TransactionValidationException extends RuntimeException {

    private final String errorCode;
    private final String transId;

    public TransactionValidationException(String errorCode, String transId, String message) {
        super(message);
        this.errorCode = errorCode;
        this.transId = transId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getTransId() {
        return transId;
    }
}
