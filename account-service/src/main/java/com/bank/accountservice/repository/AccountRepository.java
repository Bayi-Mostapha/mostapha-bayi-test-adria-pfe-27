package com.bank.accountservice.repository;

import com.bank.accountservice.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByCustomerReference(String customerReference);
}
