package com.clbs.investment.controller;

import com.clbs.investment.dto.HistoryInquiryResponse;
import com.clbs.investment.service.PortfolioInquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST controller replacing COBOL CICS program: INQHIST.
 *
 * COBOL CICS → REST mapping:
 *   EXEC CICS RECEIVE MAP('HISTMAP') function=H → GET /api/history/{accountNo}
 *   READ TRANHIST (by account/date range)        → query params: fundId, from, to
 *   EXEC CICS SEND MAP response                  → JSON response body
 */
@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
@Tag(name = "History", description = "Transaction history inquiry (INQHIST)")
public class HistoryController {

    private final PortfolioInquiryService portfolioInquiryService;

    /**
     * COBOL: INQHIST — retrieve transaction history.
     * Optional fundId filters to a single fund (replaces COBOL GENERIC key).
     * Optional from/to filters by date range (replaces COBOL date comparison loop).
     */
    @GetMapping("/{accountNo}")
    @Operation(summary = "Get transaction history (INQHIST)")
    public ResponseEntity<HistoryInquiryResponse> getHistory(
            @PathVariable Long accountNo,
            @RequestParam(required = false) String fundId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(portfolioInquiryService.getHistory(accountNo, fundId, from, to));
    }
}
