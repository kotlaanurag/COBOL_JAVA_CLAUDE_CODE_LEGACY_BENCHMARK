package com.clbs.investment.service;

import com.clbs.investment.domain.entity.PositionMaster;
import com.clbs.investment.domain.entity.TransactionRecord;
import com.clbs.investment.domain.enums.AccountStatus;
import com.clbs.investment.exception.PositionUpdateException;
import com.clbs.investment.repository.PositionMasterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * Migrated from COBOL batch program: POSUPD00 (Position Update).
 *
 * COBOL flow:
 *  1. READ POSMSTRE KEY IS WS-ACCOUNT-NO WS-FUND-ID
 *  2. IF NOT FOUND AND transaction type = BY → create new position record
 *  3. PERFORM UPDATE-COST-BASIS
 *     - Buy:  new avgCost = (oldBalance*oldAvgCost + qty*price) / newBalance
 *     - Sell: avgCost unchanged; costBasis = newBalance * avgCost
 *     - Fee:  deduct from cost basis only
 *  4. REWRITE POSMSTRE
 *
 * Optimistic locking (@Version on PositionMaster) replaces COBOL VSAM ENQ/DEQ
 * to prevent concurrent update conflicts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PositionUpdateService {

    private final PositionMasterRepository positionMasterRepository;
    private final ErrorLoggingService errorLoggingService;

    private static final String PROGRAM_ID = "POSUPD00";
    private static final int SCALE = 4;

    /**
     * Applies a validated transaction to the POSMSTRE position record.
     * Returns the before-balance (for HISTLD00 audit trail).
     *
     * COBOL: REWRITE POSMSTRE
     */
    @Transactional
    public BigDecimal applyTransaction(TransactionRecord tx) {
        Optional<PositionMaster> existing = positionMasterRepository
                .findByAccountNoAndFundId(tx.getAccountNo(), tx.getFundId());

        PositionMaster pos;
        BigDecimal beforeBalance;

        if (existing.isPresent()) {
            pos = existing.get();
            beforeBalance = pos.getShareBalance();
        } else {
            // New position — only valid for Buy (TRNVAL00 already enforced this)
            pos = new PositionMaster();
            pos.setAccountNo(tx.getAccountNo());
            pos.setFundId(tx.getFundId());
            pos.setShareBalance(BigDecimal.ZERO);
            pos.setAverageCost(BigDecimal.ZERO);
            pos.setCostBasis(BigDecimal.ZERO);
            pos.setStatus(AccountStatus.A);
            beforeBalance = BigDecimal.ZERO;
            log.info("[{}] Creating new position account={} fund={}", PROGRAM_ID, tx.getAccountNo(), tx.getFundId());
        }

        switch (tx.getTransactionType()) {
            case BY -> applyBuy(pos, tx);
            case SL -> applySell(pos, tx);
            case FE -> applyFee(pos, tx);
        }

        pos.setLastTranDate(tx.getTranDate());
        pos.setLastTransId(tx.getTransId());

        try {
            positionMasterRepository.save(pos);
            log.debug("[{}] Updated position account={} fund={} newBalance={}",
                    PROGRAM_ID, tx.getAccountNo(), tx.getFundId(), pos.getShareBalance());
        } catch (Exception e) {
            errorLoggingService.logError(PROGRAM_ID, "E009", tx.getAccountNo(), tx.getFundId(), tx.getTransId(),
                    "Position update failed: " + e.getMessage());
            throw new PositionUpdateException(tx.getAccountNo(), tx.getFundId(), "POSMSTRE REWRITE failed", e);
        }

        return beforeBalance;
    }

    /**
     * COBOL: PERFORM UPDATE-COST-BASIS (Buy).
     * newAvgCost = (oldBalance * oldAvgCost + qty * price) / newBalance
     */
    private void applyBuy(PositionMaster pos, TransactionRecord tx) {
        BigDecimal oldValue = pos.getShareBalance().multiply(pos.getAverageCost());
        BigDecimal newValue = tx.getShareQty().multiply(tx.getSharePrice());
        BigDecimal newBalance = pos.getShareBalance().add(tx.getShareQty());

        BigDecimal newAvgCost = newBalance.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : oldValue.add(newValue).divide(newBalance, SCALE, RoundingMode.HALF_UP);

        pos.setShareBalance(newBalance);
        pos.setAverageCost(newAvgCost);
        pos.setCostBasis(newBalance.multiply(newAvgCost).setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * COBOL: PERFORM UPDATE-COST-BASIS (Sell).
     * avgCost is unchanged; costBasis = newBalance * avgCost
     */
    private void applySell(PositionMaster pos, TransactionRecord tx) {
        BigDecimal newBalance = pos.getShareBalance().subtract(tx.getShareQty());
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new PositionUpdateException(pos.getAccountNo(), pos.getFundId(),
                    "Sell would result in negative balance (E003 should have caught this)");
        }
        pos.setShareBalance(newBalance);
        pos.setCostBasis(newBalance.multiply(pos.getAverageCost()).setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * COBOL: PERFORM UPDATE-COST-BASIS (Fee).
     * Fee reduces cost basis only; share balance unchanged.
     */
    private void applyFee(PositionMaster pos, TransactionRecord tx) {
        BigDecimal newCostBasis = pos.getCostBasis().subtract(tx.getTranAmount());
        pos.setCostBasis(newCostBasis.max(BigDecimal.ZERO));
    }
}
