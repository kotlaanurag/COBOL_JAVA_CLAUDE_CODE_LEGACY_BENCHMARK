package com.clbs.investment.dto;

import com.clbs.investment.domain.enums.BatchStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for batch job status inquiry (replaces COBOL BCHCTL00 status display).
 */
@Data
@Builder
public class BatchStatusResponse {

    private LocalDate processDate;
    private String processId;
    private BatchStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long recordCount;
    private long errorCount;
    private long lastPosition;
    private String returnCode;
    private String statusMessage;
}
