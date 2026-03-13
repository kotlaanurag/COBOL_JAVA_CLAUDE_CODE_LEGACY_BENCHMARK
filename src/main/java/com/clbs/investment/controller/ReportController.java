package com.clbs.investment.controller;

import com.clbs.investment.domain.entity.ErrorLog;
import com.clbs.investment.domain.entity.PositionHistory;
import com.clbs.investment.domain.entity.PositionMaster;
import com.clbs.investment.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST controller for report generation.
 * Replaces COBOL batch report programs: RPTPOS00, RPTAUD00, RPTSTA00.
 * COBOL wrote to SYSOUT line printer; here the data is returned as JSON.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Report generation (RPTPOS00/RPTAUD00/RPTSTA00)")
public class ReportController {

    private final ReportService reportService;

    /** COBOL: RPTPOS00 — daily position report grouped by account */
    @GetMapping("/positions/daily")
    @Operation(summary = "Daily position report (RPTPOS00)")
    public ResponseEntity<Map<Long, List<PositionMaster>>> dailyPositionReport() {
        return ResponseEntity.ok(reportService.generateDailyPositionReport());
    }

    /** COBOL: RPTPOS00 — position history snapshot for a specific date (DB2 POSHIST) */
    @GetMapping("/positions/history")
    @Operation(summary = "Position history snapshot (RPTPOS00/POSHIST)")
    public ResponseEntity<List<PositionHistory>> positionHistoryReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(reportService.generatePositionHistoryReport(date));
    }

    /** COBOL: RPTAUD00 — security audit report from ERRLOG */
    @GetMapping("/audit")
    @Operation(summary = "Audit report (RPTAUD00)")
    public ResponseEntity<List<ErrorLog>> auditReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(reportService.generateAuditReport(from, to));
    }

    /** COBOL: RPTSTA00 — statistics and error counts per program */
    @GetMapping("/statistics")
    @Operation(summary = "Statistics report (RPTSTA00)")
    public ResponseEntity<Map<String, Long>> statisticsReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(reportService.generateStatisticsReport(from, to));
    }
}
