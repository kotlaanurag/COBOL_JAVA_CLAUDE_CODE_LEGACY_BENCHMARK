package com.clbs.investment.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Configuration;

/**
 * Enables Spring Batch infrastructure.
 *
 * Spring Batch replaces the z/OS JCL batch scheduling layer:
 *  - JobRepository (DB-backed) replaces COBOL BCHCTL VSAM checkpoint file
 *  - JobLauncher replaces z/OS EXEC PGM= JCL statements
 *  - Step fault-tolerance replaces COBOL COND= parameters
 *
 * The default Spring Batch schema is auto-created for H2 (dev).
 * For production PostgreSQL, set spring.batch.jdbc.initialize-schema=always.
 */
@Configuration
@EnableBatchProcessing
public class BatchConfig {
    // Spring Boot 3.x auto-configures JobRepository, JobLauncher, and JobExplorer.
    // Custom overrides can be added here if needed (e.g., custom DataSource for batch tables).
}
