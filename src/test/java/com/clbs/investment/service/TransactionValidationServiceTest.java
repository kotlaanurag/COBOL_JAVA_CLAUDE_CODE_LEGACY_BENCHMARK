package com.clbs.investment.service;

import com.clbs.investment.domain.entity.PositionMaster;
import com.clbs.investment.domain.entity.TransactionRecord;
import com.clbs.investment.domain.enums.AccountStatus;
import com.clbs.investment.domain.enums.TransactionType;
import com.clbs.investment.exception.TransactionValidationException;
import com.clbs.investment.repository.PositionMasterRepository;
import com.clbs.investment.repository.TransactionRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionValidationService (COBOL TRNVAL00).
 * Tests mirror the COBOL test data specifications from test-data-specs.md.
 */
@ExtendWith(MockitoExtension.class)
class TransactionValidationServiceTest {

    @Mock
    private PositionMasterRepository positionMasterRepository;
    @Mock
    private TransactionRecordRepository transactionRecordRepository;
    @Mock
    private ErrorLoggingService errorLoggingService;

    @InjectMocks
    private TransactionValidationService service;

    private TransactionRecord validBuy;
    private PositionMaster existingPosition;

    @BeforeEach
    void setUp() {
        validBuy = new TransactionRecord();
        validBuy.setTransId("TXN000000001");
        validBuy.setAccountNo(123456789L);
        validBuy.setFundId("FND001");
        validBuy.setTransactionType(TransactionType.BY);
        validBuy.setTranDate(LocalDate.now());
        validBuy.setShareQty(new BigDecimal("100.0000"));
        validBuy.setSharePrice(new BigDecimal("25.5000"));
        validBuy.setTranAmount(new BigDecimal("2550.00"));
        validBuy.setStatus("P");

        existingPosition = new PositionMaster();
        existingPosition.setAccountNo(123456789L);
        existingPosition.setFundId("FND001");
        existingPosition.setShareBalance(new BigDecimal("500.0000"));
        existingPosition.setStatus(AccountStatus.A);

        when(transactionRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void validBuy_returnsValidStatus() {
        when(positionMasterRepository.findByAccountNoAndFundId(anyLong(), anyString()))
                .thenReturn(Optional.of(existingPosition));
        when(transactionRecordRepository.existsByTransId(anyString())).thenReturn(false);

        TransactionRecord result = service.validate(validBuy);

        assertThat(result.getStatus()).isEqualTo("V");
    }

    @Test
    void futureDateTransaction_throwsE002() {
        validBuy.setTranDate(LocalDate.now().plusDays(1));

        assertThatThrownBy(() -> service.validate(validBuy))
                .isInstanceOf(TransactionValidationException.class)
                .hasFieldOrPropertyWithValue("errorCode", "E002");
    }

    @Test
    void sellWithoutExistingPosition_throwsE001() {
        TransactionRecord sell = validBuy;
        sell.setTransactionType(TransactionType.SL);
        when(positionMasterRepository.findByAccountNoAndFundId(anyLong(), anyString()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.validate(sell))
                .isInstanceOf(TransactionValidationException.class)
                .hasFieldOrPropertyWithValue("errorCode", "E001");
    }

    @Test
    void sellExceedingBalance_throwsE003() {
        TransactionRecord sell = validBuy;
        sell.setTransactionType(TransactionType.SL);
        sell.setShareQty(new BigDecimal("9999.0000")); // exceeds balance of 500

        when(positionMasterRepository.findByAccountNoAndFundId(anyLong(), anyString()))
                .thenReturn(Optional.of(existingPosition));

        assertThatThrownBy(() -> service.validate(sell))
                .isInstanceOf(TransactionValidationException.class)
                .hasFieldOrPropertyWithValue("errorCode", "E003");
    }

    @Test
    void duplicateTransId_returnsWarningStatus() {
        when(positionMasterRepository.findByAccountNoAndFundId(anyLong(), anyString()))
                .thenReturn(Optional.of(existingPosition));
        when(transactionRecordRepository.existsByTransId(anyString())).thenReturn(true);

        TransactionRecord result = service.validate(validBuy);

        assertThat(result.getStatus()).isEqualTo("W");
        verify(errorLoggingService).logError(eq("TRNVAL00"), eq("W001"), anyLong(), anyString(), anyString(), anyString());
    }
}
