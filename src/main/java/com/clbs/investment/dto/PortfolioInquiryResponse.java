package com.clbs.investment.dto;

import com.clbs.investment.domain.enums.AccountStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Response DTO for portfolio inquiry (replaces COBOL CICS INQPORT screen map).
 *
 * COBOL COMMAREA fields:
 *  WS-ACCOUNT-NO, WS-FUNCTION (P=Position), COMMAREA-POSITIONS list
 */
@Data
@Builder
public class PortfolioInquiryResponse {

    private Long accountNo;
    private List<PositionSummary> positions;
    private int totalPositions;

    @Data
    @Builder
    public static class PositionSummary {
        private String fundId;
        private String cusip;
        private BigDecimal shareBalance;
        private BigDecimal averageCost;
        private BigDecimal costBasis;
        private LocalDate lastTranDate;
        private String lastTransId;
        private AccountStatus status;
    }
}
