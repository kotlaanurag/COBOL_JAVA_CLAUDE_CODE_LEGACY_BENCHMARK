package com.clbs.investment.repository;

import com.clbs.investment.domain.entity.PositionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Replaces COBOL DB2 table POSHIST SQL operations.
 * HISTLD00 inserts daily position snapshots.
 * RPTPOS00 queries for report generation.
 */
@Repository
public interface PositionHistoryRepository extends JpaRepository<PositionHistory, Long> {

    /** COBOL DB2: SELECT * FROM POSHIST WHERE ACCOUNT_NO=? AND FUND_ID=? AND TRANS_DATE=? */
    Optional<PositionHistory> findByAccountNoAndFundIdAndTransDate(Long accountNo, String fundId, LocalDate transDate);

    /** COBOL DB2: SELECT * FROM POSHIST WHERE ACCOUNT_NO=? AND FUND_ID=? ORDER BY TRANS_DATE DESC */
    List<PositionHistory> findByAccountNoAndFundIdOrderByTransDateDesc(Long accountNo, String fundId);

    /** COBOL DB2: RPTPOS00 report — all positions for a given date */
    List<PositionHistory> findByTransDateOrderByAccountNoAscFundIdAsc(LocalDate transDate);
}
