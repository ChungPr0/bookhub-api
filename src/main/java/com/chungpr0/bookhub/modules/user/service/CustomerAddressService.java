package com.chungpr0.bookhub.modules.user.service;

import com.chungpr0.bookhub.modules.user.dto.request.AddressRequest;
import com.chungpr0.bookhub.modules.user.dto.response.AddressResponse;
import com.chungpr0.bookhub.modules.user.dto.response.DeleteAddressResponse;
import com.chungpr0.bookhub.modules.user.dto.response.SetDefaultAddressResponse;

import java.util.List;

public interface CustomerAddressService {

    List<AddressResponse> getAddresses(Long accountId);

    AddressResponse getAddressById(Long accountId, Long addressId);

    AddressResponse createAddress(Long accountId, AddressRequest request);

    AddressResponse updateAddress(Long accountId, Long addressId, AddressRequest request);

    DeleteAddressResponse deleteAddress(Long accountId, Long addressId);

    SetDefaultAddressResponse setDefaultAddress(Long accountId, Long addressId);
}

