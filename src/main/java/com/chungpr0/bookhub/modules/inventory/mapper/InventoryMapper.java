package com.chungpr0.bookhub.modules.inventory.mapper;

import com.chungpr0.bookhub.modules.catalog.entity.Book;
import com.chungpr0.bookhub.modules.inventory.dto.response.BatchResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.InventoryTransactionResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.SimpleBookResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.SimpleReferenceResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.SimpleSupplierResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.SimpleUserResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.StockReceiptDetailResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.StockReceiptItemResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.StockReceiptSummaryResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.SupplierDetailResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.SupplierResponse;
import com.chungpr0.bookhub.modules.inventory.entity.Batch;
import com.chungpr0.bookhub.modules.inventory.entity.InventoryTransaction;
import com.chungpr0.bookhub.modules.inventory.entity.StockReceipt;
import com.chungpr0.bookhub.modules.inventory.entity.Supplier;

import java.util.ArrayList;
import java.util.List;

public final class InventoryMapper {

    private InventoryMapper() {
    }

    public static SupplierResponse toSupplierResponse(Supplier supplier, Long receiptCount, Long totalImportCost) {
        if (supplier == null) {
            return null;
        }
        return SupplierResponse.builder()
                .id(supplier.getId())
                .name(supplier.getName())
                .contactName(supplier.getContactName())
                .phone(supplier.getPhone())
                .email(supplier.getEmail())
                .address(supplier.getAddress())
                .taxCode(supplier.getTaxCode())
                .receiptCount(receiptCount != null ? receiptCount : 0L)
                .totalImportCost(totalImportCost != null ? totalImportCost : 0L)
                .createdAt(supplier.getCreatedAt())
                .updatedAt(supplier.getUpdatedAt())
                .build();
    }

    public static SupplierDetailResponse toSupplierDetailResponse(Supplier supplier, Long receiptCount, Long totalImportCost) {
        if (supplier == null) {
            return null;
        }
        return SupplierDetailResponse.builder()
                .id(supplier.getId())
                .name(supplier.getName())
                .contactName(supplier.getContactName())
                .phone(supplier.getPhone())
                .email(supplier.getEmail())
                .address(supplier.getAddress())
                .taxCode(supplier.getTaxCode())
                .receiptCount(receiptCount != null ? receiptCount : 0L)
                .totalImportCost(totalImportCost != null ? totalImportCost : 0L)
                .createdAt(supplier.getCreatedAt())
                .updatedAt(supplier.getUpdatedAt())
                .build();
    }

    public static SimpleSupplierResponse toSimpleSupplierResponse(Supplier supplier) {
        if (supplier == null) {
            return null;
        }
        return SimpleSupplierResponse.builder()
                .id(supplier.getId())
                .name(supplier.getName())
                .build();
    }

    public static SimpleBookResponse toSimpleBookResponse(Book book) {
        if (book == null) {
            return null;
        }
        return SimpleBookResponse.builder()
                .id(book.getId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .slug(book.getSlug())
                .thumbnailUrl(book.getThumbnailUrl())
                .build();
    }

    public static SimpleUserResponse toSimpleUserResponse(Long accountId, String fullName) {
        if (accountId == null) {
            return null;
        }
        return SimpleUserResponse.builder()
                .id(accountId)
                .fullName(fullName)
                .build();
    }

    public static StockReceiptItemResponse toStockReceiptItemResponse(Batch batch) {
        if (batch == null) {
            return null;
        }
        long lineTotal = batch.getImportPrice() * batch.getQuantityImported();
        return StockReceiptItemResponse.builder()
                .batchId(batch.getId())
                .batchCode(batch.getBatchCode())
                .book(toSimpleBookResponse(batch.getBook()))
                .importPrice(batch.getImportPrice())
                .quantityImported(batch.getQuantityImported())
                .quantityRemaining(batch.getQuantityRemaining())
                .lineTotal(lineTotal)
                .build();
    }

    public static StockReceiptSummaryResponse toStockReceiptSummaryResponse(StockReceipt receipt, String creatorName) {
        if (receipt == null) {
            return null;
        }
        return StockReceiptSummaryResponse.builder()
                .id(receipt.getId())
                .receiptCode(receipt.getReceiptCode())
                .supplier(toSimpleSupplierResponse(receipt.getSupplier()))
                .note(receipt.getNote())
                .importDate(receipt.getImportDate())
                .totalQuantity(receipt.getTotalQuantity())
                .totalCost(receipt.getTotalCost())
                .createdBy(toSimpleUserResponse(receipt.getCreatedBy(), creatorName))
                .createdAt(receipt.getCreatedAt())
                .build();
    }

    public static StockReceiptDetailResponse toStockReceiptDetailResponse(StockReceipt receipt, String creatorName) {
        if (receipt == null) {
            return null;
        }
        List<StockReceiptItemResponse> items = new ArrayList<>();
        if (receipt.getItems() != null) {
            for (Batch batch : receipt.getItems()) {
                items.add(toStockReceiptItemResponse(batch));
            }
        }
        return StockReceiptDetailResponse.builder()
                .id(receipt.getId())
                .receiptCode(receipt.getReceiptCode())
                .supplier(toSimpleSupplierResponse(receipt.getSupplier()))
                .note(receipt.getNote())
                .importDate(receipt.getImportDate())
                .totalQuantity(receipt.getTotalQuantity())
                .totalCost(receipt.getTotalCost())
                .createdBy(toSimpleUserResponse(receipt.getCreatedBy(), creatorName))
                .createdAt(receipt.getCreatedAt())
                .items(items)
                .build();
    }

    public static BatchResponse toBatchResponse(Batch batch) {
        if (batch == null) {
            return null;
        }
        StockReceipt receipt = batch.getReceipt();
        Supplier supplier = (receipt != null) ? receipt.getSupplier() : null;
        String receiptCode = (receipt != null) ? receipt.getReceiptCode() : null;

        return BatchResponse.builder()
                .id(batch.getId())
                .batchCode(batch.getBatchCode())
                .book(toSimpleBookResponse(batch.getBook()))
                .receiptCode(receiptCode)
                .supplier(toSimpleSupplierResponse(supplier))
                .importPrice(batch.getImportPrice())
                .quantityImported(batch.getQuantityImported())
                .quantityRemaining(batch.getQuantityRemaining())
                .importDate(batch.getImportDate())
                .createdAt(batch.getCreatedAt())
                .build();
    }

    public static InventoryTransactionResponse toInventoryTransactionResponse(InventoryTransaction transaction, String creatorName) {
        if (transaction == null) {
            return null;
        }
        String refCode = null;
        if (transaction.getReferenceType() != null && transaction.getReferenceId() != null) {
            if ("RECEIPT".equalsIgnoreCase(transaction.getReferenceType())) {
                refCode = "REC-" + transaction.getReferenceId();
            } else if ("ORDER".equalsIgnoreCase(transaction.getReferenceType())) {
                refCode = "ORD-" + transaction.getReferenceId();
            } else if ("ADJUSTMENT".equalsIgnoreCase(transaction.getReferenceType())) {
                refCode = "ADJ-" + transaction.getReferenceId();
            } else {
                refCode = transaction.getReferenceType() + "-" + transaction.getReferenceId();
            }
        }

        SimpleReferenceResponse ref = null;
        if (transaction.getReferenceType() != null || transaction.getReferenceId() != null) {
            ref = SimpleReferenceResponse.builder()
                    .type(transaction.getReferenceType())
                    .id(transaction.getReferenceId())
                    .code(refCode)
                    .build();
        }

        return InventoryTransactionResponse.builder()
                .id(transaction.getId())
                .type(transaction.getType())
                .quantity(transaction.getQuantity())
                .stockAfter(transaction.getStockAfter())
                .reference(ref)
                .reason(transaction.getReason())
                .note(transaction.getNote())
                .createdBy(toSimpleUserResponse(transaction.getCreatedBy(), creatorName))
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}

