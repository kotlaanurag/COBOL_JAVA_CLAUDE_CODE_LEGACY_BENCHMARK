package com.clbs.investment.controller;

import com.clbs.investment.domain.entity.BatchControl;
import com.clbs.investment.dto.BatchStatusResponse;
import com.clbs.investment.repository.BatchControlRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST controller for batch job management.
 * Replaces z/OS JCL job submission and BCHCTL00 status inquiry.
 *
 * COBOL equivalent:
 *   z/OS job submit  → POST /api/batch/jobs/{jobName}/run
 *   BCHCTL00 display → GET  /api/batch/status
 */
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
@Tag(name = "Batch", description = "Batch job management (JCL/BCHCTL00 replacement)")
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job transactionProcessingJob;
    private final BatchControlRepository batchControlRepository;

    /**
     * Trigger the transaction processing pipeline.
     * Replaces z/OS: SUBMIT JCL(TRNMAIN) or scheduler trigger at 18:00.
     */
    @PostMapping("/jobs/transaction-processing/run")
    @Operation(summary = "Run transaction processing job (JCL TRNMAIN)")
    public ResponseEntity<Map<String, Object>> runTransactionJob() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();
        JobExecution execution = jobLauncher.run(transactionProcessingJob, params);
        return ResponseEntity.accepted().body(Map.of(
                "jobName", execution.getJobInstance().getJobName(),
                "executionId", execution.getId(),
                "status", execution.getStatus().name()
        ));
    }

    /**
     * Get batch control status for today.
     * Replaces BCHCTL00 display / PRCCTL status inquiry.
     */
    @GetMapping("/status")
    @Operation(summary = "Get today's batch control status (BCHCTL00)")
    public ResponseEntity<List<BatchStatusResponse>> getTodayStatus() {
        List<BatchControl> records = batchControlRepository.findByProcessDateOrderByProcessId(LocalDate.now());
        List<BatchStatusResponse> response = records.stream()
                .map(bc -> BatchStatusResponse.builder()
                        .processDate(bc.getProcessDate())
                        .processId(bc.getProcessId())
                        .status(bc.getStatus())
                        .startTime(bc.getStartTime())
                        .endTime(bc.getEndTime())
                        .recordCount(bc.getRecordCount())
                        .errorCount(bc.getErrorCount())
                        .lastPosition(bc.getLastPosition())
                        .returnCode(bc.getReturnCode())
                        .statusMessage(bc.getStatusMessage())
                        .build())
                .toList();
        return ResponseEntity.ok(response);
    }
}
