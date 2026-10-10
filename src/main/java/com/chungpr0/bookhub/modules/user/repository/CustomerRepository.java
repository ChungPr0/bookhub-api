package com.chungpr0.bookhub.modules.user.repository;

import com.chungpr0.bookhub.modules.user.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.chungpr0.bookhub.common.enums.CustomerTier;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    Optional<Customer> findByAccountId(Long accountId);

    Optional<Customer> findByPhone(String phone);

    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByPhone(String phone);

    long countByCustomerTier(CustomerTier customerTier);

    List<Customer> findByCreatedAtBetween(OffsetDateTime from, OffsetDateTime to);

    long countByCreatedAtBetween(OffsetDateTime from, OffsetDateTime to);

    List<Customer> findByOrderByTotalSpentDesc(Pageable pageable);
}
