package com.clbs.investment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Investment Portfolio Management System
 *
 * Migrated from z/OS COBOL + CICS + DB2 + VSAM to Spring Boot + Spring Batch + JPA.
 *
 * COBOL architecture mapping:
 *  - VSAM KSDS (POSMSTRE, BCHCTL)     → JPA entities backed by PostgreSQL/H2
 *  - VSAM ESDS (TRANHIST)             → Append-only JPA entity
 *  - DB2 tables (POSHIST, ERRLOG)     → JPA entities
 *  - Batch programs (TRNVAL00 etc.)   → Spring Batch jobs/steps
 *  - CICS online programs (INQPORT)   → Spring MVC REST controllers
 *  - z/OS RACF security               → Spring Security
 */
@SpringBootApplication
@EnableScheduling
public class InvestmentPortfolioApplication {

    public static void main(String[] args) {
        SpringApplication.run(InvestmentPortfolioApplication.class, args);
    }
}
