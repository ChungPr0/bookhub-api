package com.chungpr0.bookhub.modules.user.repository;

import com.chungpr0.bookhub.modules.user.entity.PointTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long>, JpaSpecificationExecutor<PointTransaction> {
}

