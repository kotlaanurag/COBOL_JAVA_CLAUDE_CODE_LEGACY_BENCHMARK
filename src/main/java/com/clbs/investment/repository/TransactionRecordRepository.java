package com.clbs.investment.repository;

import com.clbs.investment.domain.entity.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Replaces COBOL sequential file READ operations on TRANFILE.
 * TRNVAL00 reads TRANFILE sequentially — Spring Batch FlatFileItemReader handles
 * flat file input; this repository is used for staged/pre-loaded transactions.
 */
@Repository
public interface TransactionRecordRepository extends JpaRepository<TransactionRecord, Long> {

    Optional<TransactionRecord> findByTransId(String transId);

    boolean existsByTransId(String transId);

    /** Fetch all pending transactions for batch processing (replaces sequential file read) */
    List<TransactionRecord> findByStatusOrderById(String status);
}
