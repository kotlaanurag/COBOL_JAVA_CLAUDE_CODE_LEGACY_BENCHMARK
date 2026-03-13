package com.clbs.investment.domain.entity;

import com.clbs.investment.domain.enums.TransactionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Migrated from COBOL VSAM ESDS file: TRANHIST (300 bytes).
 *
 * ESDS (Entry-Sequenced Data Set) = append-only, no delete/update.
 * This entity is therefore append-only: no update/delete operations in the service layer.
 * Written by HISTLD00 (HistoryLoadService) after POSUPD00 completes successfully.
 *
 * COBOL data: timestamp, before/after balance snapshot for full audit trail.
 */
@Entity
@Table(name = "transaction_history")
@Getter
@Setter
@NoArgsConstructor
public class TransactionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** COBOL: HIST-TIMESTAMP PIC X(26). Microsecond precision timestamp */
    @Column(name = "hist_timestamp", nullable = false)
    private LocalDateTime histTimestamp;

    /** COBOL: ACCOUNT-NO PIC 9(9) */
    @NotNull
    @Column(name = "account_no", nullable = false)
    private Long accountNo;

    /** COBOL: FUND-ID PIC X(6) */
    @NotBlank
    @Column(name = "fund_id", nullable = false, length = 6)
    private String fundId;

    /** COBOL: TRANS-ID PIC X(12) */
    @NotBlank
    @Column(name = "trans_id", nullable = false, length = 12)
    private String transId;

    /** COBOL: WS-TRAN-TYPE. BY/SL/FE */
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 2)
    private TransactionType transactionType;

    /** COBOL: TRAN-DATE PIC X(10) */
    @Column(name = "tran_date", nullable = false)
    private LocalDate tranDate;

    /** COBOL: SHARE-QTY PIC S9(9)V9(4) */
    @Column(name = "share_qty", nullable = false, precision = 13, scale = 4)
    private BigDecimal shareQty;

    /** COBOL: SHARE-PRICE PIC S9(9)V9(4) */
    @Column(name = "share_price", nullable = false, precision = 13, scale = 4)
    private BigDecimal sharePrice;

    /** COBOL: TRAN-AMOUNT PIC S9(11)V9(2) */
    @Column(name = "tran_amount", nullable = false, precision = 13, scale = 2)
    private BigDecimal tranAmount;

    /** COBOL: RESULT-CODE PIC X(4). 0000/0004/0008/0012/0016 */
    @Column(name = "result_code", nullable = false, length = 4)
    private String resultCode;

    /** COBOL: BEFORE-SHARE-BAL PIC S9(13)V9(4). Snapshot before this transaction */
    @Column(name = "before_share_balance", precision = 17, scale = 4)
    private BigDecimal beforeShareBalance;

    /** COBOL: AFTER-SHARE-BAL PIC S9(13)V9(4). Snapshot after this transaction */
    @Column(name = "after_share_balance", precision = 17, scale = 4)
    private BigDecimal afterShareBalance;
}
