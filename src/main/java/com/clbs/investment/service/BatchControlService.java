package com.clbs.investment.service;

import com.clbs.investment.domain.entity.BatchControl;
import com.clbs.investment.domain.enums.BatchStatus;
import com.clbs.investment.domain.enums.ReturnCode;
import com.clbs.investment.repository.BatchControlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Migrated from COBOL programs: BCHCTL00 (Batch Control) and PRCCTL (Process Sequencer).
 *
 * COBOL responsibilities:
 *  - BCHCTL00: reads/writes BCHCTL VSAM KSDS for job status tracking
 *  - PRCCTL:   defines process sequence and dependency checks
 *  - CKPRST:   checkpoint/restart — resume from lastPosition on job restart
 *
 * Job execution order (from data-dictionary.md scheduling):
 *   1. TRNVAL00  18:00–18:15  (prerequisite: day open)
 *   2. POSUPD00  18:15–19:00  (prerequisite: TRNVAL00 RC <= 0004)
 *   3. HISTLD00  19:00–19:30  (prerequisite: POSUPD00 RC <= 0004)
 *   4. RPTGEN00  19:30–20:00  (no prerequisite)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BatchControlService {

    private final BatchControlRepository batchControlRepository;

    /**
     * COBOL: BCHCTL00 START-PROCESS — mark a job as In-Process.
     * Creates the control record if it doesn't exist (first run).
     */
    @Transactional
    public BatchControl startProcess(String processId) {
        LocalDate today = LocalDate.now();
        BatchControl ctrl = batchControlRepository
                .findByProcessDateAndProcessId(today, processId)
                .orElseGet(() -> {
                    BatchControl bc = new BatchControl();
                    bc.setProcessDate(today);
                    bc.setProcessId(processId);
                    return bc;
                });

        ctrl.setStatus(BatchStatus.P);
        ctrl.setStartTime(LocalDateTime.now());
        ctrl.setEndTime(null);
        ctrl.setReturnCode(null);
        log.info("[BCHCTL00] Started processId={} date={}", processId, today);
        return batchControlRepository.save(ctrl);
    }

    /**
     * COBOL: BCHCTL00 COMPLETE-PROCESS — mark a job as Complete with return code.
     */
    @Transactional
    public BatchControl completeProcess(String processId, ReturnCode returnCode,
                                        long recordCount, long errorCount) {
        BatchControl ctrl = getOrThrow(processId);
        ctrl.setStatus(returnCode.getCode() <= 4 ? BatchStatus.C : BatchStatus.E);
        ctrl.setEndTime(LocalDateTime.now());
        ctrl.setReturnCode(String.format("%04d", returnCode.getCode()));
        ctrl.setRecordCount(recordCount);
        ctrl.setErrorCount(errorCount);
        ctrl.setStatusMessage(returnCode.getDescription());
        log.info("[BCHCTL00] Completed processId={} RC={} records={} errors={}",
                processId, returnCode.getCode(), recordCount, errorCount);
        return batchControlRepository.save(ctrl);
    }

    /**
     * COBOL: CKPRST — save checkpoint position for restart capability.
     * Called every 1000–2000 records per COBOL convention.
     */
    @Transactional
    public void saveCheckpoint(String processId, long position, String lastTransId) {
        BatchControl ctrl = getOrThrow(processId);
        ctrl.setLastPosition(position);
        ctrl.setLastTransId(lastTransId);
        batchControlRepository.save(ctrl);
        log.debug("[CKPRST] Checkpoint processId={} position={} transId={}", processId, position, lastTransId);
    }

    /**
     * COBOL: PRCCTL dependency check — verify prerequisite job completed with acceptable RC.
     * Returns true if the prerequisite is satisfied (RC <= maxAllowedCode).
     */
    @Transactional(readOnly = true)
    public boolean isDependencySatisfied(String prerequisiteProcessId, int maxAllowedReturnCode) {
        Optional<BatchControl> prereq = batchControlRepository
                .findByProcessDateAndProcessId(LocalDate.now(), prerequisiteProcessId);

        if (prereq.isEmpty()) {
            log.warn("[PRCCTL] Prerequisite {} not found for today", prerequisiteProcessId);
            return false;
        }

        BatchControl ctrl = prereq.get();
        if (ctrl.getStatus() != BatchStatus.C && ctrl.getStatus() != BatchStatus.E) {
            log.warn("[PRCCTL] Prerequisite {} not yet complete (status={})", prerequisiteProcessId, ctrl.getStatus());
            return false;
        }

        int rc = ctrl.getReturnCode() != null ? Integer.parseInt(ctrl.getReturnCode()) : 9999;
        boolean satisfied = rc <= maxAllowedReturnCode;
        if (!satisfied) {
            log.error("[PRCCTL] Prerequisite {} RC={} exceeds max allowed {}", prerequisiteProcessId, rc, maxAllowedReturnCode);
        }
        return satisfied;
    }

    private BatchControl getOrThrow(String processId) {
        return batchControlRepository
                .findByProcessDateAndProcessId(LocalDate.now(), processId)
                .orElseThrow(() -> new IllegalStateException("No BatchControl record for processId=" + processId));
    }
}
