package com.clbs.investment.service;

import com.clbs.investment.domain.entity.ErrorLog;
import com.clbs.investment.domain.entity.PositionHistory;
import com.clbs.investment.domain.entity.PositionMaster;
import com.clbs.investment.domain.enums.AccountStatus;
import com.clbs.investment.repository.ErrorLogRepository;
import com.clbs.investment.repository.PositionHistoryRepository;
import com.clbs.investment.repository.PositionMasterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Migrated from COBOL batch report programs: RPTPOS00, RPTAUD00, RPTSTA00.
 *
 * COBOL report programs read from VSAM and DB2 and write to SYSOUT (line printer).
 * Here each method returns structured data; controllers can render as JSON or CSV.
 *
 * RPTPOS00: Daily position report with valuations
 * RPTAUD00: Security audit report and exception report
 * RPTSTA00: System performance statistics and trend analysis
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final PositionMasterRepository positionMasterRepository;
    private final PositionHistoryRepository positionHistoryRepository;
    private final ErrorLogRepository errorLogRepository;

    /**
     * COBOL: RPTPOS00 — Daily position report.
     * Reads all active positions from POSMSTRE and daily snapshots from POSHIST.
     * Returns summary grouped by account number.
     */
    @Transactional(readOnly = true)
    public Map<Long, List<PositionMaster>> generateDailyPositionReport() {
        log.info("[RPTPOS00] Generating daily position report for {}", LocalDate.now());
        List<PositionMaster> allActive = positionMasterRepository.findAllActivePositions(AccountStatus.A);
        return allActive.stream().collect(Collectors.groupingBy(PositionMaster::getAccountNo));
    }

    /**
     * COBOL: RPTPOS00 — Position history snapshot for a specific date (DB2 POSHIST query).
     */
    @Transactional(readOnly = true)
    public List<PositionHistory> generatePositionHistoryReport(LocalDate date) {
        log.info("[RPTPOS00] Generating position history report for date={}", date);
        return positionHistoryRepository.findByTransDateOrderByAccountNoAscFundIdAsc(date);
    }

    /**
     * COBOL: RPTAUD00 — Security audit report.
     * Returns error log entries within a date/time range for audit review.
     * In COBOL this was a printed report; here it is structured data.
     */
    @Transactional(readOnly = true)
    public List<ErrorLog> generateAuditReport(LocalDateTime from, LocalDateTime to) {
        log.info("[RPTAUD00] Generating audit report from={} to={}", from, to);
        return errorLogRepository.findByErrorTimestampBetweenOrderByErrorTimestampDesc(from, to);
    }

    /**
     * COBOL: RPTSTA00 — System statistics report.
     * Returns error counts per program for trend analysis.
     */
    @Transactional(readOnly = true)
    public Map<String, Long> generateStatisticsReport(LocalDateTime from, LocalDateTime to) {
        log.info("[RPTSTA00] Generating statistics report from={} to={}", from, to);
        List<ErrorLog> errors = errorLogRepository.findByErrorTimestampBetweenOrderByErrorTimestampDesc(from, to);

        Map<String, Long> countsByProgram = errors.stream()
                .collect(Collectors.groupingBy(ErrorLog::getProgramId, Collectors.counting()));

        BigDecimal totalPositionValue = positionMasterRepository
                .findAllActivePositions(AccountStatus.A)
                .stream()
                .map(PositionMaster::getCostBasis)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("[RPTSTA00] Total portfolio cost basis: {}", totalPositionValue);
        return countsByProgram;
    }
}
