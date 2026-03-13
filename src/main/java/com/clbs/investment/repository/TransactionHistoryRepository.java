package com.clbs.investment.repository;

import com.clbs.investment.domain.entity.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Replaces COBOL VSAM ESDS TRANHIST sequential write (WRITE only — no update/delete).
 * HISTLD00 (HistoryLoadService) appends records; INQHIST reads them for inquiry.
 *
 * ESDS constraint: no update or delete operations are permitted through this repository.
 */
@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {

    /** COBOL: INQHIST reads by account and optional date range */
    List<TransactionHistory> findByAccountNoOrderByHistTimestampDesc(Long accountNo);

    List<TransactionHistory> findByAccountNoAndFundIdOrderByHistTimestampDesc(Long accountNo, String fundId);

    List<TransactionHistory> findByAccountNoAndTranDateBetweenOrderByHistTimestampDesc(
            Long accountNo, LocalDate from, LocalDate to);

    boolean existsByTransId(String transId);
}
