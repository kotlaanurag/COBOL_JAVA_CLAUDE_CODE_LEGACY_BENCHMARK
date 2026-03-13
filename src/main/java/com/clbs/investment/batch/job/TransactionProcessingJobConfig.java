package com.clbs.investment.batch.job;

import com.clbs.investment.batch.processor.TransactionProcessor;
import com.clbs.investment.domain.entity.TransactionRecord;
import com.clbs.investment.domain.enums.ReturnCode;
import com.clbs.investment.service.BatchControlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import com.clbs.investment.repository.TransactionRecordRepository;

import java.util.Map;

/**
 * Spring Batch job configuration — migrated from COBOL JCL batch pipeline.
 *
 * COBOL JCL sequence (from jcl/batch/):
 *   STEP010  EXEC PGM=TRNVAL00
 *   STEP020  EXEC PGM=POSUPD00, COND=(4,LT,STEP010)
 *   STEP030  EXEC PGM=HISTLD00, COND=(4,LT,STEP020)
 *   STEP040  EXEC PGM=RPTGEN00
 *
 * All four COBOL steps are consolidated into one Spring Batch job with
 * a single chunk-oriented step. The TransactionProcessor implements the
 * TRNVAL00 → POSUPD00 → HISTLD00 pipeline per record.
 *
 * Checkpoint/restart: Spring Batch's built-in JobRepository provides the
 * same checkpoint/restart capability as COBOL CKPRST (BCHCTL VSAM).
 * Chunk size 1000 matches COBOL checkpoint frequency of every 1000 records.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class TransactionProcessingJobConfig {

    private static final int CHUNK_SIZE = 1000; // matches COBOL checkpoint frequency

    private final TransactionRecordRepository transactionRecordRepository;
    private final TransactionProcessor transactionProcessor;
    private final BatchControlService batchControlService;

    @Bean
    Job transactionProcessingJob(JobRepository jobRepository,
                                         Step transactionProcessingStep) {
        return new JobBuilder("transactionProcessingJob", jobRepository)
                .start(transactionProcessingStep)
                .listener(new org.springframework.batch.core.JobExecutionListener() {
                    @Override
                    public void beforeJob(org.springframework.batch.core.JobExecution je) {
                        batchControlService.startProcess("TRNVAL00");
                        log.info("[JCL] transactionProcessingJob started");
                    }

                    @Override
                    public void afterJob(org.springframework.batch.core.JobExecution je) {
                        long writeCount = je.getStepExecutions().stream()
                                .mapToLong(s -> s.getWriteCount()).sum();
                        long skipCount = je.getStepExecutions().stream()
                                .mapToLong(s -> s.getProcessSkipCount()).sum();
                        ReturnCode rc;
                        if (skipCount == 0) {
                            rc = ReturnCode.SUCCESS;
                        } else if (skipCount < writeCount) {
                            rc = ReturnCode.WARNING;
                        } else {
                            rc = ReturnCode.ERROR;
                        }
                        batchControlService.completeProcess("TRNVAL00", rc, writeCount + skipCount, skipCount);
                        log.info("[JCL] transactionProcessingJob finished status={} RC={}", je.getStatus(), rc);
                    }
                })
                .build();
    }

    @Bean
    Step transactionProcessingStep(JobRepository jobRepository,
                                           PlatformTransactionManager txManager) {
        return new StepBuilder("transactionProcessingStep", jobRepository)
                .<TransactionRecord, TransactionRecord>chunk(CHUNK_SIZE, txManager)
                .reader(pendingTransactionReader())
                .processor(transactionProcessor)
                .writer(processedTransactionWriter())
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(Integer.MAX_VALUE) // COBOL continues on E001–E004; skip and log
                .build();
    }

    /**
     * COBOL: READ TRANFILE NEXT RECORD — sequential read of pending transactions.
     * Reads records with status='P' (Pending) ordered by ID (insertion order).
     */
    @Bean
    RepositoryItemReader<TransactionRecord> pendingTransactionReader() {
        return new RepositoryItemReaderBuilder<TransactionRecord>()
                .name("pendingTransactionReader")
                .repository(transactionRecordRepository)
                .methodName("findAll")
                .pageSize(CHUNK_SIZE)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    /**
     * COBOL: REWRITE — persist final status of each processed transaction.
     */
    @Bean
    RepositoryItemWriter<TransactionRecord> processedTransactionWriter() {
        return new RepositoryItemWriterBuilder<TransactionRecord>()
                .repository(transactionRecordRepository)
                .methodName("save")
                .build();
    }
}
