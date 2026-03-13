package com.clbs.investment.service;

import com.clbs.investment.domain.entity.PositionHistory;
import com.clbs.investment.domain.entity.TransactionHistory;
import com.clbs.investment.domain.entity.TransactionRecord;
import com.clbs.investment.repository.PositionHistoryRepository;
import com.clbs.investment.repository.TransactionHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Migrated from COBOL batch program: HISTLD00 (History Load).
 *
 * COBOL flow:
 *  1. WRITE TRANHIST record (VSAM ESDS — append only)
 *  2. EXEC SQL INSERT INTO POSHIST ... END-EXEC  (DB2 daily snapshot)
 *  3. EXEC SQL COMMIT END-EXEC  (DB2CMT)
 *
 * Called after POSUPD00 successfully applies each transaction.
 * Both writes are in the same Spring transaction — matching COBOL's
 * single unit-of-work commitment (DB2CMT after both VSAM and DB2 writes).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HistoryLoadService {

    private final TransactionHistoryRepository transactionHistoryRepository;
    private final PositionHistoryRepository positionHistoryRepository;

    private static final String PROGRAM_ID = "HISTLD00";

    /**
     * Writes the transaction audit record (TRANHIST) and daily position snapshot (POSHIST).
     *
     * @param tx             validated transaction record from TRNVAL00
     * @param beforeBalance  share balance before the transaction (captured by POSUPD00)
     * @param afterBalance   share balance after the transaction
     * @param costBasis      updated cost basis after the transaction
     * @param avgCost        updated average cost after the transaction
     */
    @Transactional
    public void load(TransactionRecord tx, BigDecimal beforeBalance,
                     BigDecimal afterBalance, BigDecimal costBasis, BigDecimal avgCost) {

        // 1. COBOL: WRITE TRANHIST (VSAM ESDS append-only)
        TransactionHistory hist = new TransactionHistory();
        hist.setHistTimestamp(LocalDateTime.now());
        hist.setAccountNo(tx.getAccountNo());
        hist.setFundId(tx.getFundId());
        hist.setTransId(tx.getTransId());
        hist.setTransactionType(tx.getTransactionType());
        hist.setTranDate(tx.getTranDate());
        hist.setShareQty(tx.getShareQty());
        hist.setSharePrice(tx.getSharePrice());
        hist.setTranAmount(tx.getTranAmount());
        hist.setResultCode("0000");
        hist.setBeforeShareBalance(beforeBalance);
        hist.setAfterShareBalance(afterBalance);
        transactionHistoryRepository.save(hist);

        // 2. COBOL: INSERT INTO POSHIST (DB2 — upsert daily snapshot)
        PositionHistory snapshot = positionHistoryRepository
                .findByAccountNoAndFundIdAndTransDate(tx.getAccountNo(), tx.getFundId(), tx.getTranDate())
                .orElseGet(() -> {
                    PositionHistory ph = new PositionHistory();
                    ph.setAccountNo(tx.getAccountNo());
                    ph.setFundId(tx.getFundId());
                    ph.setTransDate(tx.getTranDate());
                    return ph;
                });

        snapshot.setShareBalance(afterBalance);
        snapshot.setCostBasis(costBasis);
        snapshot.setAvgCost(avgCost);
        snapshot.setProcTimestamp(LocalDateTime.now());
        positionHistoryRepository.save(snapshot);

        log.debug("[{}] Loaded history transId={} account={} fund={}",
                PROGRAM_ID, tx.getTransId(), tx.getAccountNo(), tx.getFundId());
    }
}
