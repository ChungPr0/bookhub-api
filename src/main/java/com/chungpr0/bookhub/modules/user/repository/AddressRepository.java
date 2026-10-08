package com.chungpr0.bookhub.modules.user.repository;

import com.chungpr0.bookhub.modules.user.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByCustomerIdOrderByIsDefaultDescUpdatedAtDesc(Long customerId);

    Optional<Address> findByIdAndCustomerId(Long id, Long customerId);

    long countByCustomerId(Long customerId);

    Optional<Address> findFirstByCustomerIdAndIdNotOrderByUpdatedAtDesc(Long customerId, Long id);

    Optional<Address> findFirstByCustomerIdOrderByUpdatedAtDesc(Long customerId);

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.customer.id = :customerId")
    void resetDefaultAddressForCustomer(@Param("customerId") Long customerId);
}

