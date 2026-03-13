package com.clbs.investment.domain.entity;

import com.clbs.investment.domain.enums.AccountStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Migrated from COBOL VSAM KSDS file: POSMSTRE (250 bytes).
 *
 * COBOL key: ACCOUNT-NO (9 digits) + FUND-ID (6 chars) = composite primary key.
 * Stores current share balance, average cost, and cost basis per account/fund.
 *
 * Validation rules from data-dictionary.md:
 *  - Account number range: 100000000–999999999
 *  - Share balance cannot go negative
 *  - Average cost and cost basis recalculated on every Buy/Sell
 */
@Entity
@Table(name = "position_master",
        uniqueConstraints = @UniqueConstraint(columnNames = {"account_no", "fund_id"}))
@Getter
@Setter
@NoArgsConstructor
public class PositionMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** COBOL: ACCOUNT-NO PIC 9(9). Range 100000000–999999999 */
    @NotNull
    @Min(100000000L)
    @Max(999999999L)
    @Column(name = "account_no", nullable = false)
    private Long accountNo;

    /** COBOL: FUND-ID PIC X(6). Alphanumeric fund identifier */
    @NotBlank
    @Size(min = 1, max = 6)
    @Column(name = "fund_id", nullable = false, length = 6)
    private String fundId;

    /** COBOL: CUSIP PIC X(9). 9-character security identifier */
    @Size(max = 9)
    @Column(name = "cusip", length = 9)
    private String cusip;

    /**
     * COBOL: SHARE-BALANCE PIC S9(13)V9(4) COMP-3.
     * Current share balance. Cannot go negative (validation rule).
     */
    @NotNull
    @DecimalMin(value = "0.0000", message = "Share balance cannot be negative")
    @Column(name = "share_balance", nullable = false, precision = 17, scale = 4)
    private BigDecimal shareBalance = BigDecimal.ZERO;

    /**
     * COBOL: AVG-COST PIC S9(9)V9(4) COMP-3.
     * Average cost per share, recalculated on each Buy.
     */
    @Column(name = "average_cost", precision = 13, scale = 4)
    private BigDecimal averageCost = BigDecimal.ZERO;

    /**
     * COBOL: COST-BASIS PIC S9(13)V9(2) COMP-3.
     * Total cost basis = shareBalance * averageCost.
     */
    @Column(name = "cost_basis", precision = 15, scale = 2)
    private BigDecimal costBasis = BigDecimal.ZERO;

    /** COBOL: LAST-TRAN-DATE PIC X(10). Date of last transaction */
    @Column(name = "last_tran_date")
    private LocalDate lastTranDate;

    /** COBOL: LAST-TRANS-ID PIC X(12). Last processed transaction ID */
    @Size(max = 12)
    @Column(name = "last_trans_id", length = 12)
    private String lastTransId;

    /** COBOL: WS-ACCOUNT-STATUS. A=Active, I=Inactive, C=Closed */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 1)
    private AccountStatus status = AccountStatus.A;

    @Version
    @Column(name = "version")
    private Long version;
}
