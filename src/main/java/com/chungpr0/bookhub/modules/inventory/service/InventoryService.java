package com.chungpr0.bookhub.modules.inventory.service;

import com.chungpr0.bookhub.modules.inventory.dto.request.AdjustInventoryRequest;
import com.chungpr0.bookhub.modules.inventory.dto.request.InventoryTransactionFilterRequest;
import com.chungpr0.bookhub.modules.inventory.dto.request.LowStockFilterRequest;
import com.chungpr0.bookhub.modules.inventory.dto.response.InventoryAdjustmentResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.InventoryTransactionPageResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.LowStockPageResponse;
import org.springframework.data.domain.Pageable;

public interface InventoryService {

    InventoryAdjustmentResponse adjustInventory(Long accountId, String idempotencyKey, AdjustInventoryRequest request);

    InventoryTransactionPageResponse getTransactions(InventoryTransactionFilterRequest filter, Pageable pageable);

    LowStockPageResponse getLowStockBooks(LowStockFilterRequest filter, Pageable pageable);
}

