package com.clbs.investment.controller;

import com.clbs.investment.dto.PortfolioInquiryResponse;
import com.clbs.investment.dto.TransactionDto;
import com.clbs.investment.domain.entity.TransactionRecord;
import com.clbs.investment.service.PortfolioInquiryService;
import com.clbs.investment.repository.TransactionRecordRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller replacing COBOL CICS programs: INQPORT and INQONLN (portfolio functions).
 *
 * COBOL CICS → REST mapping:
 *   EXEC CICS RECEIVE MAP('PORTMAP') → POST /api/portfolio/inquiry
 *   READ POSMSTRE (generic key)      → GET  /api/portfolio/{accountNo}
 *   EXEC CICS SEND MAP response      → JSON response body
 *
 * Transaction submission replaces batch TRANFILE flat-file input.
 */
@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
@Tag(name = "Portfolio", description = "Portfolio inquiry and transaction submission (INQPORT/INQONLN)")
public class PortfolioController {

    private final PortfolioInquiryService portfolioInquiryService;
    private final TransactionRecordRepository transactionRecordRepository;

    /**
     * COBOL: INQPORT — retrieve all active positions for an account.
     * Replaces CICS map PORTMAP with JSON response.
     */
    @GetMapping("/{accountNo}")
    @Operation(summary = "Get portfolio positions (INQPORT)", description = "Returns all active positions for an account")
    public ResponseEntity<PortfolioInquiryResponse> getPortfolio(@PathVariable Long accountNo) {
        return ResponseEntity.ok(portfolioInquiryService.getPortfolio(accountNo));
    }

    /**
     * Submit a transaction for batch processing.
     * Replaces writing a record to the COBOL TRANFILE sequential input file.
     * The record will be picked up by the next transactionProcessingJob run.
     */
    @PostMapping("/transactions")
    @Operation(summary = "Submit transaction (TRANFILE input)", description = "Stages a Buy/Sell/Fee transaction for batch processing")
    public ResponseEntity<TransactionRecord> submitTransaction(@Valid @RequestBody TransactionDto dto) {
        TransactionRecord tx = new TransactionRecord();
        tx.setTransId(dto.getTransId());
        tx.setAccountNo(dto.getAccountNo());
        tx.setFundId(dto.getFundId());
        tx.setTransactionType(dto.getTransactionType());
        tx.setTranDate(dto.getTranDate());
        tx.setShareQty(dto.getShareQty());
        tx.setSharePrice(dto.getSharePrice());
        tx.setTranAmount(dto.getTranAmount());
        tx.setStatus("P");
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionRecordRepository.save(tx));
    }
}
