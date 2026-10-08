package com.chungpr0.bookhub.common.enums;

import java.util.Collections;
import java.util.List;

public enum Permission {
    CATALOG_READ,
    CATALOG_WRITE,
    INVENTORY_READ,
    INVENTORY_WRITE,
    ORDER_READ,
    ORDER_PROCESS,
    ORDER_FINANCE,
    CUSTOMER_READ,
    CUSTOMER_WRITE,
    STAFF_MANAGE,
    VOUCHER_READ,
    VOUCHER_WRITE,
    PAYMENT_METHOD_MANAGE,
    REVIEW_MODERATE,
    REPORT_VIEW;

    public static List<String> getPermissionsByRole(Role role) {
        if (role == null) {
            return Collections.emptyList();
        }
        return switch (role) {
            case ADMIN -> List.of(
                    CATALOG_READ.name(),
                    CATALOG_WRITE.name(),
                    INVENTORY_READ.name(),
                    INVENTORY_WRITE.name(),
                    ORDER_READ.name(),
                    ORDER_PROCESS.name(),
                    ORDER_FINANCE.name(),
                    CUSTOMER_READ.name(),
                    CUSTOMER_WRITE.name(),
                    STAFF_MANAGE.name(),
                    VOUCHER_READ.name(),
                    VOUCHER_WRITE.name(),
                    PAYMENT_METHOD_MANAGE.name(),
                    REVIEW_MODERATE.name(),
                    REPORT_VIEW.name()
            );
            case MANAGER -> List.of(
                    CATALOG_READ.name(),
                    CATALOG_WRITE.name(),
                    INVENTORY_READ.name(),
                    INVENTORY_WRITE.name(),
                    ORDER_READ.name(),
                    ORDER_PROCESS.name(),
                    ORDER_FINANCE.name(),
                    CUSTOMER_READ.name(),
                    CUSTOMER_WRITE.name(),
                    VOUCHER_READ.name(),
                    VOUCHER_WRITE.name(),
                    REVIEW_MODERATE.name(),
                    REPORT_VIEW.name()
            );
            case STAFF -> List.of(
                    CATALOG_READ.name(),
                    INVENTORY_READ.name(),
                    INVENTORY_WRITE.name(),
                    ORDER_READ.name(),
                    ORDER_PROCESS.name(),
                    CUSTOMER_READ.name(),
                    REVIEW_MODERATE.name()
            );
            case CUSTOMER -> Collections.emptyList();
        };
    }
}

