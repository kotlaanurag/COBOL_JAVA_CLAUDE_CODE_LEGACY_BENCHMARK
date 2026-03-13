package com.clbs.investment.service;

import com.clbs.investment.domain.entity.ErrorLog;
import com.clbs.investment.repository.ErrorLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Migrated from COBOL DB2ERR and ERRPROC routines.
 *
 * COBOL: PERFORM DB2ERR — inserts a row into DB2 ERRLOG table.
 * Uses PROPAGATION_REQUIRES_NEW so error logging always commits,
 * even if the outer transaction rolls back (matching COBOL behaviour
 * where DB2ERR ran as an independent unit of work).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ErrorLoggingService {

    private final ErrorLogRepository errorLogRepository;

    /**
     * COBOL: DB2ERR — log an error to ERRLOG table.
     *
     * @param programId  COBOL program name, e.g. "TRNVAL00"
     * @param errorCode  E001–E004 or W001–W002
     * @param accountNo  nullable — null for system-level errors
     * @param fundId     nullable
     * @param transId    nullable
     * @param description human-readable error description
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logError(String programId, String errorCode,
                         Long accountNo, String fundId, String transId,
                         String description) {
        ErrorLog entry = new ErrorLog();
        entry.setErrorTimestamp(LocalDateTime.now());
        entry.setProgramId(programId);
        entry.setErrorCode(errorCode);
        entry.setAccountNo(accountNo);
        entry.setFundId(fundId);
        entry.setTransId(transId);
        entry.setErrorDesc(description);
        errorLogRepository.save(entry);
        log.warn("[{}] {} account={} fund={} trans={}: {}", programId, errorCode, accountNo, fundId, transId, description);
    }
}
