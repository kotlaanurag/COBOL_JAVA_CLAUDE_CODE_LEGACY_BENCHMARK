package com.clbs.investment.dto;

import com.clbs.investment.domain.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for history inquiry (replaces COBOL CICS INQHIST screen map).
 *
 * COBOL COMMAREA: WS-FUNCTION=H, WS-ACCOUNT-NO, WS-FROM-DATE, WS-TO-DATE
 */
@Data
@Builder
public class HistoryInquiryResponse {

    private Long accountNo;
    private String fundId;
    private LocalDate fromDate;
    private LocalDate toDate;
    private List<HistoryEntry> transactions;
    private int totalRecords;

    @Data
    @Builder
    public static class HistoryEntry {
        private LocalDateTime histTimestamp;
        private String transId;
        private TransactionType transactionType;
        private LocalDate tranDate;
        private BigDecimal shareQty;
        private BigDecimal sharePrice;
        private BigDecimal tranAmount;
        private BigDecimal beforeShareBalance;
        private BigDecimal afterShareBalance;
        private String resultCode;
    }
}
