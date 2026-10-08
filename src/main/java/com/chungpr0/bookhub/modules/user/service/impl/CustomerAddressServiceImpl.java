package com.chungpr0.bookhub.modules.user.service.impl;

import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.common.util.PhoneUtils;
import com.chungpr0.bookhub.modules.user.dto.request.AddressRequest;
import com.chungpr0.bookhub.modules.user.dto.response.AddressResponse;
import com.chungpr0.bookhub.modules.user.dto.response.DeleteAddressResponse;
import com.chungpr0.bookhub.modules.user.dto.response.SetDefaultAddressResponse;
import com.chungpr0.bookhub.modules.user.entity.Address;
import com.chungpr0.bookhub.modules.user.entity.Customer;
import com.chungpr0.bookhub.modules.user.repository.AddressRepository;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import com.chungpr0.bookhub.modules.user.service.CustomerAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerAddressServiceImpl implements CustomerAddressService {

    private static final int MAX_ADDRESS_LIMIT = 10;

    private final AddressRepository addressRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getAddresses(Long accountId) {
        Customer customer = getCustomer(accountId);
        List<Address> addresses = addressRepository.findByCustomerIdOrderByIsDefaultDescUpdatedAtDesc(customer.getId());
        return addresses.stream().map(AddressResponse::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getAddressById(Long accountId, Long addressId) {
        Customer customer = getCustomer(accountId);
        Address address = addressRepository.findByIdAndCustomerId(addressId, customer.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));

        return AddressResponse.fromEntity(address);
    }

    @Override
    @Transactional
    public AddressResponse createAddress(Long accountId, AddressRequest request) {
        Customer customer = getCustomer(accountId);

        long currentCount = addressRepository.countByCustomerId(customer.getId());
        if (currentCount >= MAX_ADDRESS_LIMIT) {
            throw new AppException(ErrorCode.ADDRESS_LIMIT_EXCEEDED);
        }

        String normalizedPhone = PhoneUtils.normalize(request.getReceiverPhone());
        boolean isDefault;

        if (currentCount == 0) {
            isDefault = true;
        } else if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.resetDefaultAddressForCustomer(customer.getId());
            isDefault = true;
        } else {
            isDefault = false;
        }

        Address address = Address.builder()
                .customer(customer)
                .receiverName(request.getReceiverName().trim())
                .receiverPhone(normalizedPhone)
                .province(request.getProvince().trim())
                .district(request.getDistrict().trim())
                .ward(request.getWard().trim())
                .detailAddress(request.getDetailAddress().trim())
                .isDefault(isDefault)
                .build();

        Address saved = addressRepository.save(address);
        return AddressResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(Long accountId, Long addressId, AddressRequest request) {
        Customer customer = getCustomer(accountId);
        Address address = addressRepository.findByIdAndCustomerId(addressId, customer.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));

        String normalizedPhone = PhoneUtils.normalize(request.getReceiverPhone());

        if (Boolean.TRUE.equals(request.getIsDefault()) && !address.isDefault()) {
            addressRepository.resetDefaultAddressForCustomer(customer.getId());
            address.setDefault(true);
        } else if (Boolean.FALSE.equals(request.getIsDefault()) && address.isDefault()) {
            // Keep default if it's currently the default to preserve the invariant of having 1 default address
            address.setDefault(true);
        }

        address.setReceiverName(request.getReceiverName().trim());
        address.setReceiverPhone(normalizedPhone);
        address.setProvince(request.getProvince().trim());
        address.setDistrict(request.getDistrict().trim());
        address.setWard(request.getWard().trim());
        address.setDetailAddress(request.getDetailAddress().trim());

        Address saved = addressRepository.save(address);
        return AddressResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public DeleteAddressResponse deleteAddress(Long accountId, Long addressId) {
        Customer customer = getCustomer(accountId);
        Address address = addressRepository.findByIdAndCustomerId(addressId, customer.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));

        boolean wasDefault = address.isDefault();
        addressRepository.delete(address);
        addressRepository.flush();

        Long newDefaultAddressId = null;
        if (wasDefault) {
            Optional<Address> nextDefaultOpt = addressRepository.findFirstByCustomerIdOrderByUpdatedAtDesc(customer.getId());
            if (nextDefaultOpt.isPresent()) {
                Address nextDefault = nextDefaultOpt.get();
                nextDefault.setDefault(true);
                addressRepository.save(nextDefault);
                newDefaultAddressId = nextDefault.getId();
            }
        }

        return DeleteAddressResponse.builder()
                .deletedAddressId(addressId)
                .newDefaultAddressId(newDefaultAddressId)
                .build();
    }

    @Override
    @Transactional
    public SetDefaultAddressResponse setDefaultAddress(Long accountId, Long addressId) {
        Customer customer = getCustomer(accountId);
        Address address = addressRepository.findByIdAndCustomerId(addressId, customer.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));

        if (!address.isDefault()) {
            addressRepository.resetDefaultAddressForCustomer(customer.getId());
            address.setDefault(true);
            addressRepository.save(address);
        }

        return SetDefaultAddressResponse.builder()
                .id(addressId)
                .isDefault(true)
                .build();
    }

    private Customer getCustomer(Long accountId) {
        return customerRepository.findByAccountId(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));
    }
}

