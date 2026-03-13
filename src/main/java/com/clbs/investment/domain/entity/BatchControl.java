package com.clbs.investment.domain.entity;

import com.clbs.investment.domain.enums.BatchStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Migrated from COBOL VSAM KSDS file: BCHCTL (200 bytes).
 *
 * COBOL key: PROCESS-DATE (8) + PROCESS-ID (8) = composite unique key.
 * Tracks batch job execution status, checkpoint position, and error counts.
 * Enables checkpoint/restart — if a job fails, restart from last-checkpoint.
 *
 * Process IDs from data-dictionary.md: TRNVAL00, POSUPD00, HISTLD00, RPTGEN00
 */
@Entity
@Table(name = "batch_control",
        uniqueConstraints = @UniqueConstraint(columnNames = {"process_date", "process_id"}))
@Getter
@Setter
@NoArgsConstructor
public class BatchControl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** COBOL: PROCESS-DATE PIC X(8). YYYYMMDD format */
    @NotNull
    @Column(name = "process_date", nullable = false)
    private LocalDate processDate;

    /**
     * COBOL: PROCESS-ID PIC X(8). One of: TRNVAL00, POSUPD00, HISTLD00, RPTGEN00.
     * Maps to Spring Batch job names.
     */
    @NotBlank
    @Size(max = 8)
    @Column(name = "process_id", nullable = false, length = 8)
    private String processId;

    /** COBOL: WS-PROCESS-STATUS. W=Waiting, P=In-Process, C=Complete, E=Error */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 1)
    private BatchStatus status = BatchStatus.W;

    /** COBOL: START-TIME TIMESTAMP */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /** COBOL: END-TIME TIMESTAMP */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /** COBOL: RECORD-COUNT PIC 9(9). Total records processed (success + rejected) */
    @Column(name = "record_count", nullable = false)
    private Long recordCount = 0L;

    /** COBOL: ERROR-COUNT PIC 9(5). Records rejected with E001–E004 */
    @Column(name = "error_count", nullable = false)
    private Long errorCount = 0L;

    /**
     * COBOL: LAST-POSITION PIC 9(9). Checkpoint: last successfully processed record number.
     * On restart, processing resumes from lastPosition + 1.
     */
    @Column(name = "last_position", nullable = false)
    private Long lastPosition = 0L;

    /**
     * COBOL: RETURN-CODE PIC 9(4). 0000/0004/0008/0012/0016.
     * Set at job completion.
     */
    @Column(name = "return_code", length = 4)
    private String returnCode;

    /** COBOL: STATUS-MESSAGE PIC X(50) */
    @Size(max = 200)
    @Column(name = "status_message", length = 200)
    private String statusMessage;

    /** Checkpoint: last processed transaction ID (for restart) */
    @Size(max = 12)
    @Column(name = "last_trans_id", length = 12)
    private String lastTransId;
}
