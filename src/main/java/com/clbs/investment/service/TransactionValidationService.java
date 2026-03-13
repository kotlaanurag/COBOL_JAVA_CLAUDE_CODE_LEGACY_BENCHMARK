package com.clbs.investment.service;

import com.clbs.investment.domain.entity.PositionMaster;
import com.clbs.investment.domain.entity.TransactionRecord;
import com.clbs.investment.domain.enums.TransactionType;
import com.clbs.investment.exception.TransactionValidationException;
import com.clbs.investment.repository.PositionMasterRepository;
import com.clbs.investment.repository.TransactionRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Migrated from COBOL batch program: TRNVAL00 (Transaction Validation).
 *
 * COBOL flow:
 *  1. READ TRANFILE INTO WS-TRAN-RECORD
 *  2. PERFORM VALIDATE-ACCOUNT-FUND
 *  3. PERFORM VALIDATE-DATE
 *  4. PERFORM VALIDATE-BALANCE  (sell only)
 *  5. PERFORM VALIDATE-DUPLICATE
 *  6. IF errors → PERFORM ERRPROC, mark rejected
 *     ELSE → mark valid, pass to POSUPD00
 *
 * Error codes:
 *  E001 = Account/fund not found in POSMSTRE
 *  E002 = Future transaction date
 *  E003 = Sell would cause negative balance
 *  E004 = Invalid transaction type
 *  W001 = Duplicate transaction ID (warning, still processes)
 *  W002 = Missing CUSIP (warning)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionValidationService {

    private final PositionMasterRepository positionMasterRepository;
    private final TransactionRecordRepository transactionRecordRepository;
    private final ErrorLoggingService errorLoggingService;

    private static final String PROGRAM_ID = "TRNVAL00";

    /**
     * Validates a single transaction record.
     * Returns the validated TransactionRecord (status="V") or throws
     * TransactionValidationException for critical errors (E001–E004).
     *
     * Warnings (W001, W002) are logged but the record is still returned as valid.
     */
    @Transactional
    public TransactionRecord validate(TransactionRecord tx) {
        log.debug("[{}] Validating transId={}", PROGRAM_ID, tx.getTransId());

        // E004: Validate transaction type (enum handles this at parse time; defensive check)
        if (tx.getTransactionType() == null) {
            logAndReject(tx, "E004", "Invalid or missing transaction type");
        }

        // E002: No future dates
        if (tx.getTranDate().isAfter(LocalDate.now())) {
            logAndReject(tx, "E002", "Transaction date " + tx.getTranDate() + " is in the future");
        }

        // E001: Account/fund must exist in POSMSTRE (or it's a new Buy creating first position)
        Optional<PositionMaster> position = positionMasterRepository
                .findByAccountNoAndFundId(tx.getAccountNo(), tx.getFundId());

        if (position.isEmpty() && tx.getTransactionType() != TransactionType.BY) {
            logAndReject(tx, "E001",
                    "Account " + tx.getAccountNo() + " / Fund " + tx.getFundId() + " not found and transaction is not a Buy");
        }

        // E003: Sell cannot result in negative balance
        if (tx.getTransactionType() == TransactionType.SL && position.isPresent()) {
            if (position.get().getShareBalance().compareTo(tx.getShareQty()) < 0) {
                logAndReject(tx, "E003",
                        "Sell qty " + tx.getShareQty() + " exceeds balance " + position.get().getShareBalance());
            }
        }

        // W001: Duplicate transaction ID
        if (transactionRecordRepository.existsByTransId(tx.getTransId())) {
            errorLoggingService.logError(PROGRAM_ID, "W001",
                    tx.getAccountNo(), tx.getFundId(), tx.getTransId(),
                    "Duplicate transaction ID — processing with warning");
            tx.setStatus("W");
            return transactionRecordRepository.save(tx);
        }

        // W002: Missing CUSIP on a new position
        if (position.isEmpty() && (tx.getTransactionType() == TransactionType.BY)) {
            // CUSIP is optional on input — POSMSTRE will store null
            log.warn("[{}] W002 Missing CUSIP for new position account={} fund={}", PROGRAM_ID, tx.getAccountNo(), tx.getFundId());
            errorLoggingService.logError(PROGRAM_ID, "W002",
                    tx.getAccountNo(), tx.getFundId(), tx.getTransId(), "Missing CUSIP for new position");
        }

        tx.setStatus("V");
        log.debug("[{}] transId={} VALID", PROGRAM_ID, tx.getTransId());
        return transactionRecordRepository.save(tx);
    }

    private void logAndReject(TransactionRecord tx, String errorCode, String message) {
        errorLoggingService.logError(PROGRAM_ID, errorCode,
                tx.getAccountNo(), tx.getFundId(), tx.getTransId(), message);
        tx.setStatus("R");
        tx.setErrorMessage(message);
        transactionRecordRepository.save(tx);
        throw new TransactionValidationException(errorCode, tx.getTransId(), message);
    }
}
