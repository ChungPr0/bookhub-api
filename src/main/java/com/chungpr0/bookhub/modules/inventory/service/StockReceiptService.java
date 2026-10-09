package com.chungpr0.bookhub.modules.inventory.service;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.modules.inventory.dto.request.BatchFilterRequest;
import com.chungpr0.bookhub.modules.inventory.dto.request.CreateStockReceiptRequest;
import com.chungpr0.bookhub.modules.inventory.dto.request.StockReceiptFilterRequest;
import com.chungpr0.bookhub.modules.inventory.dto.response.BatchResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.CreateStockReceiptResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.StockReceiptDetailResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.StockReceiptSummaryResponse;
import org.springframework.data.domain.Pageable;

public interface StockReceiptService {

    CreateStockReceiptResponse createStockReceipt(Long accountId, String idempotencyKey, CreateStockReceiptRequest request);

    PageResponse<StockReceiptSummaryResponse> getStockReceipts(StockReceiptFilterRequest filter, Pageable pageable);

    StockReceiptDetailResponse getStockReceiptDetail(Long id);

    PageResponse<BatchResponse> getBatches(BatchFilterRequest filter, Pageable pageable);
}

