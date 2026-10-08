package com.chungpr0.bookhub.modules.order.service;

import com.chungpr0.bookhub.modules.order.dto.request.CalculateShippingRequest;
import com.chungpr0.bookhub.modules.order.dto.request.UpdateShippingConfigRequest;
import com.chungpr0.bookhub.modules.order.dto.response.ShippingCalculateResponse;
import com.chungpr0.bookhub.modules.order.dto.response.ShippingConfigResponse;
import com.chungpr0.bookhub.modules.order.dto.response.ShippingServiceResponse;
import com.chungpr0.bookhub.modules.order.dto.response.ShippingTrackingResponse;

import java.util.List;

public interface ShippingService {

    List<ShippingServiceResponse> getShippingServices();

    ShippingCalculateResponse calculateShipping(Long accountId, CalculateShippingRequest request);

    ShippingTrackingResponse getTracking(String orderCode);

    ShippingConfigResponse getShippingConfig();

    ShippingConfigResponse updateShippingConfig(Long accountId, UpdateShippingConfigRequest request);
}

