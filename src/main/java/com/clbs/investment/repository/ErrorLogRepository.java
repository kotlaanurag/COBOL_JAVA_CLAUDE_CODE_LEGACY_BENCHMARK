package com.clbs.investment.repository;

import com.clbs.investment.domain.entity.ErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Replaces COBOL DB2 table ERRLOG SQL INSERT operations (DB2ERR).
 * Records are only inserted, never updated or deleted (audit requirement).
 */
@Repository
public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {

    List<ErrorLog> findByProgramIdOrderByErrorTimestampDesc(String programId);

    List<ErrorLog> findByAccountNoOrderByErrorTimestampDesc(Long accountNo);

    List<ErrorLog> findByErrorTimestampBetweenOrderByErrorTimestampDesc(
            LocalDateTime from, LocalDateTime to);

    long countByProgramIdAndErrorTimestampBetween(String programId, LocalDateTime from, LocalDateTime to);
}
