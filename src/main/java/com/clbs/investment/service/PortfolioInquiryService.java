package com.clbs.investment.service;

import com.clbs.investment.domain.entity.PositionMaster;
import com.clbs.investment.domain.entity.TransactionHistory;
import com.clbs.investment.domain.enums.AccountStatus;
import com.clbs.investment.dto.HistoryInquiryResponse;
import com.clbs.investment.dto.PortfolioInquiryResponse;
import com.clbs.investment.repository.PositionMasterRepository;
import com.clbs.investment.repository.TransactionHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Migrated from COBOL CICS online programs: INQPORT and INQHIST.
 *
 * COBOL flow (INQPORT):
 *  1. EXEC CICS RECEIVE MAP('PORTMAP') ... END-EXEC
 *  2. READ POSMSTRE KEY IS WS-ACCOUNT-NO (GENERIC — all funds)
 *  3. EXEC CICS SEND MAP('PORTMAP') ... END-EXEC
 *
 * COBOL flow (INQHIST):
 *  1. EXEC CICS RECEIVE MAP('HISTMAP') ... END-EXEC
 *  2. READ TRANHIST by account (+ optional date range)
 *  3. EXEC CICS SEND MAP('HISTMAP') ... END-EXEC
 *
 * The CICS map send/receive is replaced by REST JSON serialization.
 * COMMAREA function codes (P=Position, H=History) are replaced by separate endpoints.
 */
@Service
@RequiredArgsConstructor
public class PortfolioInquiryService {

    private final PositionMasterRepository positionMasterRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;

    /**
     * COBOL: INQPORT — retrieve all active positions for an account.
     */
    @Transactional(readOnly = true)
    public PortfolioInquiryResponse getPortfolio(Long accountNo) {
        List<PositionMaster> positions = positionMasterRepository
                .findByAccountNoAndStatus(accountNo, AccountStatus.A);

        List<PortfolioInquiryResponse.PositionSummary> summaries = positions.stream()
                .map(p -> PortfolioInquiryResponse.PositionSummary.builder()
                        .fundId(p.getFundId())
                        .cusip(p.getCusip())
                        .shareBalance(p.getShareBalance())
                        .averageCost(p.getAverageCost())
                        .costBasis(p.getCostBasis())
                        .lastTranDate(p.getLastTranDate())
                        .lastTransId(p.getLastTransId())
                        .status(p.getStatus())
                        .build())
                .toList();

        return PortfolioInquiryResponse.builder()
                .accountNo(accountNo)
                .positions(summaries)
                .totalPositions(summaries.size())
                .build();
    }

    /**
     * COBOL: INQHIST — retrieve transaction history for an account/fund with optional date range.
     */
    @Transactional(readOnly = true)
    public HistoryInquiryResponse getHistory(Long accountNo, String fundId,
                                              LocalDate fromDate, LocalDate toDate) {
        List<TransactionHistory> records;

        if (fromDate != null && toDate != null) {
            records = transactionHistoryRepository
                    .findByAccountNoAndTranDateBetweenOrderByHistTimestampDesc(accountNo, fromDate, toDate);
        } else if (fundId != null) {
            records = transactionHistoryRepository
                    .findByAccountNoAndFundIdOrderByHistTimestampDesc(accountNo, fundId);
        } else {
            records = transactionHistoryRepository
                    .findByAccountNoOrderByHistTimestampDesc(accountNo);
        }

        List<HistoryInquiryResponse.HistoryEntry> entries = records.stream()
                .map(h -> HistoryInquiryResponse.HistoryEntry.builder()
                        .histTimestamp(h.getHistTimestamp())
                        .transId(h.getTransId())
                        .transactionType(h.getTransactionType())
                        .tranDate(h.getTranDate())
                        .shareQty(h.getShareQty())
                        .sharePrice(h.getSharePrice())
                        .tranAmount(h.getTranAmount())
                        .beforeShareBalance(h.getBeforeShareBalance())
                        .afterShareBalance(h.getAfterShareBalance())
                        .resultCode(h.getResultCode())
                        .build())
                .toList();

        return HistoryInquiryResponse.builder()
                .accountNo(accountNo)
                .fundId(fundId)
                .fromDate(fromDate)
                .toDate(toDate)
                .transactions(entries)
                .totalRecords(entries.size())
                .build();
    }
}
