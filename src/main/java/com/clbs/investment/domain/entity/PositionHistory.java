package com.clbs.investment.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Migrated from COBOL DB2 table: POSHIST (Position History).
 *
 * Primary key: (ACCOUNT_NO, FUND_ID, TRANS_DATE) — matches DB2 schema.
 * Written by HISTLD00 (HistoryLoadService) to maintain daily position snapshots.
 * Used by RPTPOS00 (PositionReportService) for daily valuations.
 */
@Entity
@Table(name = "position_history",
        uniqueConstraints = @UniqueConstraint(columnNames = {"account_no", "fund_id", "trans_date"}))
@Getter
@Setter
@NoArgsConstructor
public class PositionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** COBOL DB2: ACCOUNT_NO CHAR(9) */
    @NotNull
    @Column(name = "account_no", nullable = false)
    private Long accountNo;

    /** COBOL DB2: FUND_ID CHAR(6) */
    @NotBlank
    @Column(name = "fund_id", nullable = false, length = 6)
    private String fundId;

    /** COBOL DB2: TRANS_DATE DATE */
    @NotNull
    @Column(name = "trans_date", nullable = false)
    private LocalDate transDate;

    /** COBOL DB2: SHARE_BAL DECIMAL(17,4) */
    @Column(name = "share_balance", nullable = false, precision = 17, scale = 4)
    private BigDecimal shareBalance;

    /** COBOL DB2: COST_BASIS DECIMAL(15,2) */
    @Column(name = "cost_basis", nullable = false, precision = 15, scale = 2)
    private BigDecimal costBasis;

    /** COBOL DB2: AVG_COST DECIMAL(13,4) */
    @Column(name = "avg_cost", nullable = false, precision = 13, scale = 4)
    private BigDecimal avgCost;

    /** COBOL DB2: PROC_TIMESTAMP TIMESTAMP. Set by HISTLD00 at load time */
    @Column(name = "proc_timestamp", nullable = false)
    private LocalDateTime procTimestamp;
}
