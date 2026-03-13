package com.clbs.investment.batch.processor;

import com.clbs.investment.domain.entity.PositionMaster;
import com.clbs.investment.domain.entity.TransactionRecord;
import com.clbs.investment.exception.TransactionValidationException;
import com.clbs.investment.service.HistoryLoadService;
import com.clbs.investment.service.PositionUpdateService;
import com.clbs.investment.service.TransactionValidationService;
import com.clbs.investment.repository.PositionMasterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Spring Batch ItemProcessor — implements the COBOL pipeline:
 *   TRNVAL00 → POSUPD00 → HISTLD00
 *
 * COBOL flow per record:
 *  1. PERFORM VALIDATE-TRANSACTION  (TRNVAL00)
 *  2. PERFORM UPDATE-POSITION        (POSUPD00)
 *  3. PERFORM LOAD-HISTORY           (HISTLD00)
 *
 * Returns null to skip rejected records (Spring Batch discards null items).
 * The record is already persisted with status=R by TransactionValidationService.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionProcessor implements ItemProcessor<TransactionRecord, TransactionRecord> {

    private final TransactionValidationService validationService;
    private final PositionUpdateService positionUpdateService;
    private final HistoryLoadService historyLoadService;
    private final PositionMasterRepository positionMasterRepository;

    @Override
    public TransactionRecord process(TransactionRecord tx) {
        try {
            // Step 1: TRNVAL00
            TransactionRecord validated = validationService.validate(tx);

            // Step 2: POSUPD00 — capture before-balance for audit trail
            BigDecimal beforeBalance = positionUpdateService.applyTransaction(validated);

            // Retrieve updated position for after-balance snapshot
            PositionMaster updated = positionMasterRepository
                    .findByAccountNoAndFundId(validated.getAccountNo(), validated.getFundId())
                    .orElseThrow();

            // Step 3: HISTLD00
            historyLoadService.load(validated, beforeBalance,
                    updated.getShareBalance(), updated.getCostBasis(), updated.getAverageCost());

            return validated;

        } catch (TransactionValidationException e) {
            // E001–E004: reject record, continue batch (matches COBOL behaviour)
            log.warn("Skipping rejected transaction transId={} error={}", tx.getTransId(), e.getErrorCode());
            return null;
        }
    }
}
