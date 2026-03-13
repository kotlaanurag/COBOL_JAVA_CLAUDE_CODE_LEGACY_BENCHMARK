package com.clbs.investment.dto;

import com.clbs.investment.domain.enums.TransactionType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Inbound transaction payload for REST API (replaces COBOL TRANFILE flat-file record).
 *
 * COBOL TRANFILE layout (200 bytes):
 *  ACCOUNT-NO  9(9), FUND-ID X(6), TRAN-TYPE X(2),
 *  TRAN-DATE X(10), TRANS-ID X(12), SHARE-QTY 9(9)V9(4),
 *  SHARE-PRICE 9(9)V9(4), TRAN-AMOUNT 9(11)V9(2)
 */
@Data
public class TransactionDto {

    @NotBlank(message = "Transaction ID is required")
    @Size(min = 12, max = 12, message = "Transaction ID must be exactly 12 characters")
    private String transId;

    @NotNull(message = "Account number is required")
    @Min(value = 100000000L, message = "Account number minimum is 100000000")
    @Max(value = 999999999L, message = "Account number maximum is 999999999")
    private Long accountNo;

    @NotBlank(message = "Fund ID is required")
    @Size(min = 1, max = 6, message = "Fund ID must be 1–6 characters")
    private String fundId;

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @NotNull(message = "Transaction date is required")
    private LocalDate tranDate;

    @NotNull(message = "Share quantity is required")
    @DecimalMin(value = "0.0001", message = "Share quantity must be positive")
    private BigDecimal shareQty;

    @NotNull(message = "Share price is required")
    @DecimalMin(value = "0.0001", message = "Share price must be positive")
    private BigDecimal sharePrice;

    @NotNull(message = "Transaction amount is required")
    @DecimalMin(value = "0.01", message = "Transaction amount must be positive")
    @Digits(integer = 11, fraction = 2, message = "Amount max 99,999,999,999.99")
    private BigDecimal tranAmount;
}
