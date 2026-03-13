package com.clbs.investment.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Migrated from COBOL DB2 table: ERRLOG (Error Log).
 *
 * Primary key: (ERROR_TIMESTAMP, PROGRAM_ID) — matches DB2 schema.
 * Written by DB2ERR (ErrorLoggingService) whenever any batch or online program
 * encounters a recoverable or non-recoverable error.
 *
 * Error codes from data-dictionary.md:
 *  E001 = Invalid account/fund (Critical – reject)
 *  E002 = Future transaction date (Critical – reject)
 *  E003 = Negative balance would result (Critical – reject)
 *  E004 = Invalid transaction type (Critical – reject)
 *  W001 = Duplicate transaction ID (Warning – process with log)
 *  W002 = Missing CUSIP (Warning – process with log)
 */
@Entity
@Table(name = "error_log")
@Getter
@Setter
@NoArgsConstructor
public class ErrorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** COBOL DB2: ERROR_TIMESTAMP TIMESTAMP */
    @NotNull
    @Column(name = "error_timestamp", nullable = false)
    private LocalDateTime errorTimestamp;

    /** COBOL DB2: PROGRAM_ID CHAR(8). E.g. TRNVAL00, POSUPD00 */
    @NotBlank
    @Size(max = 8)
    @Column(name = "program_id", nullable = false, length = 8)
    private String programId;

    /** COBOL DB2: ERROR_CODE CHAR(4). E001–E004, W001–W002 */
    @NotBlank
    @Size(max = 4)
    @Column(name = "error_code", nullable = false, length = 4)
    private String errorCode;

    /** COBOL DB2: ACCOUNT_NO CHAR(9). Nullable — system errors have no account */
    @Column(name = "account_no")
    private Long accountNo;

    /** COBOL DB2: FUND_ID CHAR(6). Nullable */
    @Size(max = 6)
    @Column(name = "fund_id", length = 6)
    private String fundId;

    /** COBOL DB2: TRANS_ID CHAR(12). Nullable */
    @Size(max = 12)
    @Column(name = "trans_id", length = 12)
    private String transId;

    /** COBOL DB2: ERROR_DESC VARCHAR(200) */
    @Size(max = 200)
    @Column(name = "error_desc", length = 200)
    private String errorDesc;
}
