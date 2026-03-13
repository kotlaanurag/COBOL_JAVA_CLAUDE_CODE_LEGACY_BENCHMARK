package com.clbs.investment.domain.entity;

import com.clbs.investment.domain.enums.TransactionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Migrated from COBOL Sequential file: TRANFILE (200 bytes).
 *
 * Input transaction records fed into the batch pipeline.
 * COBOL processing: TRNVAL00 reads this file, validates each record,
 * then passes valid records to POSUPD00.
 *
 * Status codes: P=Pending, V=Valid, R=Rejected, W=Warning
 */
@Entity
@Table(name = "transaction_record")
@Getter
@Setter
@NoArgsConstructor
public class TransactionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** COBOL: TRANS-ID PIC X(12). Unique 12-character transaction identifier */
    @NotBlank
    @Size(min = 12, max = 12)
    @Column(name = "trans_id", nullable = false, unique = true, length = 12)
    private String transId;

    /** COBOL: ACCOUNT-NO PIC 9(9) */
    @NotNull
    @Min(100000000L)
    @Max(999999999L)
    @Column(name = "account_no", nullable = false)
    private Long accountNo;

    /** COBOL: FUND-ID PIC X(6) */
    @NotBlank
    @Size(min = 1, max = 6)
    @Column(name = "fund_id", nullable = false, length = 6)
    private String fundId;

    /** COBOL: WS-TRAN-TYPE. BY=Buy, SL=Sell, FE=Fee */
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 2)
    private TransactionType transactionType;

    /** COBOL: TRAN-DATE PIC X(10). No future dates allowed */
    @NotNull
    @Column(name = "tran_date", nullable = false)
    private LocalDate tranDate;

    /** COBOL: SHARE-QTY PIC S9(9)V9(4) COMP-3 */
    @NotNull
    @DecimalMin(value = "0.0001")
    @Column(name = "share_qty", nullable = false, precision = 13, scale = 4)
    private BigDecimal shareQty;

    /** COBOL: SHARE-PRICE PIC S9(9)V9(4) COMP-3 */
    @NotNull
    @DecimalMin(value = "0.0001")
    @Column(name = "share_price", nullable = false, precision = 13, scale = 4)
    private BigDecimal sharePrice;

    /** COBOL: TRAN-AMOUNT PIC S9(11)V9(2) COMP-3. Max 99,999,999,999.99 */
    @NotNull
    @DecimalMin(value = "0.01")
    @Digits(integer = 11, fraction = 2)
    @Column(name = "tran_amount", nullable = false, precision = 13, scale = 2)
    private BigDecimal tranAmount;

    /**
     * Processing status:
     * P=Pending (not yet processed by TRNVAL00)
     * V=Valid (passed TRNVAL00)
     * R=Rejected (failed TRNVAL00 with error E001–E004)
     * W=Warning (processed with warning W001–W002)
     */
    @Column(name = "status", nullable = false, length = 1)
    private String status = "P";

    @Size(max = 200)
    @Column(name = "error_message", length = 200)
    private String errorMessage;
}
