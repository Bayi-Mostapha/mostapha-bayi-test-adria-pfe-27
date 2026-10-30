package com.bank.accountservice;

import com.bank.accountservice.domain.Account;
import com.bank.accountservice.domain.AccountStatus;
import com.bank.accountservice.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Fills the in-memory H2 database with the three accounts required by the exam and
 * prints their UUIDs, which are needed in every curl example.
 */
@Component
public class SeedData implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedData.class);

    private final AccountService accountService;

    public SeedData(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void run(String... args) {
        Account first = accountService.create("CUST-001", "EUR", 5000, AccountStatus.ACTIVE);
        Account second = accountService.create("CUST-002", "EUR", 3000, AccountStatus.ACTIVE);
        Account third = accountService.create("CUST-003", "EUR", 1500, AccountStatus.BLOCKED);

        log.info("Seed data created:");
        log.info("  ACTIVE  account CUST-001, EUR 5000 -> id={}", first.getId());
        log.info("  ACTIVE  account CUST-002, EUR 3000 -> id={}", second.getId());
        log.info("  BLOCKED account CUST-003, EUR 1500 -> id={}", third.getId());
    }
}
