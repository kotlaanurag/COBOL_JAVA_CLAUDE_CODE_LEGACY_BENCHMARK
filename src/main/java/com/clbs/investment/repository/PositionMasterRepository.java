package com.clbs.investment.repository;

import com.clbs.investment.domain.entity.PositionMaster;
import com.clbs.investment.domain.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Replaces COBOL VSAM KSDS POSMSTRE random-access READ/WRITE/REWRITE operations.
 *
 * COBOL key lookup:   READ POSMSTRE KEY IS WS-ACCOUNT-NO WS-FUND-ID
 * Java equivalent:    findByAccountNoAndFundId(accountNo, fundId)
 */
@Repository
public interface PositionMasterRepository extends JpaRepository<PositionMaster, Long> {

    /** COBOL: READ POSMSTRE KEY IS WS-ACCOUNT-NO WS-FUND-ID */
    Optional<PositionMaster> findByAccountNoAndFundId(Long accountNo, String fundId);

    /** COBOL: READ POSMSTRE GENERIC KEY IS WS-ACCOUNT-NO (all funds for account) */
    List<PositionMaster> findByAccountNo(Long accountNo);

    /** Find all active positions for an account (INQPORT online inquiry) */
    List<PositionMaster> findByAccountNoAndStatus(Long accountNo, AccountStatus status);

    /** COBOL: RPTPOS00 — full position sweep for daily report */
    @Query("SELECT p FROM PositionMaster p WHERE p.status = :status ORDER BY p.accountNo, p.fundId")
    List<PositionMaster> findAllActivePositions(@Param("status") AccountStatus status);

    boolean existsByAccountNoAndFundId(Long accountNo, String fundId);
}
