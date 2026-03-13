package com.clbs.investment.repository;

import com.clbs.investment.domain.entity.BatchControl;
import com.clbs.investment.domain.enums.BatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Replaces COBOL VSAM KSDS BCHCTL random-access operations.
 * BCHCTL00 (BatchControlService) reads/writes process status and checkpoint data.
 */
@Repository
public interface BatchControlRepository extends JpaRepository<BatchControl, Long> {

    /** COBOL: READ BCHCTL KEY IS WS-PROCESS-DATE WS-PROCESS-ID */
    Optional<BatchControl> findByProcessDateAndProcessId(LocalDate processDate, String processId);

    /** Find all jobs for a given run date */
    List<BatchControl> findByProcessDateOrderByProcessId(LocalDate processDate);

    /** Find jobs by status (e.g., find all failed jobs for rerun) */
    List<BatchControl> findByStatusOrderByProcessDateDescProcessIdAsc(BatchStatus status);
}
