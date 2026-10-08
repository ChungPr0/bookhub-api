package com.chungpr0.bookhub.modules.cart.mapper;

import com.chungpr0.bookhub.common.enums.BookStatus;
import com.chungpr0.bookhub.common.enums.CartItemAvailability;
import com.chungpr0.bookhub.common.enums.StockStatus;
import com.chungpr0.bookhub.modules.cart.dto.response.AdminCartResponse;
import com.chungpr0.bookhub.modules.cart.dto.response.CartItemAuthorResponse;
import com.chungpr0.bookhub.modules.cart.dto.response.CartItemResponse;
import com.chungpr0.bookhub.modules.cart.dto.response.CartResponse;
import com.chungpr0.bookhub.modules.cart.dto.response.CartSummaryResponse;
import com.chungpr0.bookhub.modules.cart.dto.response.WishlistItemResponse;
import com.chungpr0.bookhub.modules.cart.entity.Cart;
import com.chungpr0.bookhub.modules.cart.entity.CartItem;
import com.chungpr0.bookhub.modules.catalog.entity.Book;
import com.chungpr0.bookhub.modules.catalog.entity.Wishlist;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class CartMapper {

    public CartItemResponse toCartItemResponse(CartItem item) {
        Book book = item.getBook();

        CartItemAvailability availability;
        int maxPurchasable;

        if (book.getStatus() == BookStatus.INACTIVE) {
            availability = CartItemAvailability.DISCONTINUED;
            maxPurchasable = 0;
        } else if (book.getStockQuantity() <= 0) {
            availability = CartItemAvailability.OUT_OF_STOCK;
            maxPurchasable = 0;
        } else if (book.getStockQuantity() < item.getQuantity()) {
            availability = CartItemAvailability.INSUFFICIENT_STOCK;
            maxPurchasable = book.getStockQuantity();
        } else {
            availability = CartItemAvailability.AVAILABLE;
            maxPurchasable = Math.min(book.getStockQuantity(), 99);
        }

        long lineTotal = (book.getSalePrice() != null ? book.getSalePrice() : 0L) * item.getQuantity();

        String thumbnailUrl = null;
        if (book.getImages() != null && !book.getImages().isEmpty()) {
            thumbnailUrl = book.getImages().get(0).getUrl();
        }

        List<CartItemAuthorResponse> authorResponses = new ArrayList<>();
        if (book.getAuthors() != null) {
            for (var author : book.getAuthors()) {
                authorResponses.add(CartItemAuthorResponse.builder()
                        .id(author.getId())
                        .name(author.getName())
                        .slug(author.getSlug())
                        .build());
            }
        }

        return CartItemResponse.builder()
                .bookId(book.getId())
                .title(book.getTitle())
                .slug(book.getSlug())
                .thumbnailUrl(thumbnailUrl)
                .authors(authorResponses)
                .originalPrice(book.getOriginalPrice())
                .salePrice(book.getSalePrice())
                .quantity(item.getQuantity())
                .lineTotal(lineTotal)
                .availability(availability)
                .maxPurchasableQuantity(maxPurchasable)
                .addedAt(item.getAddedAt())
                .build();
    }

    public CartResponse toCartResponse(Cart cart, List<CartItem> items) {
        if (items == null) {
            items = Collections.emptyList();
        }

        List<CartItemResponse> itemResponses = new ArrayList<>();
        int totalQuantity = 0;
        long subtotal = 0L;
        long selectableSubtotal = 0L;
        boolean hasUnavailableItems = false;

        for (CartItem item : items) {
            CartItemResponse response = toCartItemResponse(item);
            itemResponses.add(response);
            totalQuantity += item.getQuantity();
            subtotal += response.getLineTotal();

            if (response.getAvailability() == CartItemAvailability.AVAILABLE) {
                selectableSubtotal += response.getLineTotal();
            } else {
                hasUnavailableItems = true;
            }
        }

        CartSummaryResponse summary = CartSummaryResponse.builder()
                .itemCount(itemResponses.size())
                .totalQuantity(totalQuantity)
                .subtotal(subtotal)
                .selectableSubtotal(selectableSubtotal)
                .hasUnavailableItems(hasUnavailableItems)
                .build();

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(itemResponses)
                .summary(summary)
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    public WishlistItemResponse toWishlistItemResponse(Wishlist wishlist) {
        Book book = wishlist.getBook();
        String thumbnailUrl = null;
        if (book.getImages() != null && !book.getImages().isEmpty()) {
            thumbnailUrl = book.getImages().get(0).getUrl();
        }

        List<CartItemAuthorResponse> authorResponses = new ArrayList<>();
        if (book.getAuthors() != null) {
            for (var author : book.getAuthors()) {
                authorResponses.add(CartItemAuthorResponse.builder()
                        .id(author.getId())
                        .name(author.getName())
                        .slug(author.getSlug())
                        .build());
            }
        }

        int discountPercent = 0;
        if (book.getOriginalPrice() != null && book.getOriginalPrice() > 0 && book.getSalePrice() != null) {
            long diff = book.getOriginalPrice() - book.getSalePrice();
            discountPercent = Math.max(0, (int) Math.round((diff * 100.0) / book.getOriginalPrice()));
        }

        StockStatus stockStatus = (book.getStockQuantity() > 0 && book.getStatus() == BookStatus.ACTIVE)
                ? StockStatus.IN_STOCK
                : StockStatus.OUT_OF_STOCK;

        boolean isBestSeller = book.getSoldCount() >= 50;

        return WishlistItemResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .slug(book.getSlug())
                .thumbnailUrl(thumbnailUrl)
                .authors(authorResponses)
                .originalPrice(book.getOriginalPrice())
                .salePrice(book.getSalePrice())
                .discountPercent(discountPercent)
                .avgRating(book.getAvgRating() != null ? book.getAvgRating() : 0.0)
                .reviewCount(book.getReviewCount())
                .stockStatus(stockStatus)
                .isBestSeller(isBestSeller)
                .addedAt(wishlist.getCreatedAt())
                .build();
    }

    public AdminCartResponse toAdminCartResponse(Cart cart) {
        List<CartItem> items = cart.getItems() != null ? cart.getItems() : Collections.emptyList();
        int totalQuantity = 0;
        long subtotal = 0L;

        for (CartItem item : items) {
            totalQuantity += item.getQuantity();
            if (item.getBook() != null && item.getBook().getSalePrice() != null) {
                subtotal += item.getBook().getSalePrice() * item.getQuantity();
            }
        }

        return AdminCartResponse.builder()
                .cartId(cart.getId())
                .customerId(cart.getCustomer() != null ? cart.getCustomer().getId() : null)
                .customerName(cart.getCustomer() != null ? cart.getCustomer().getFullName() : null)
                .customerPhone(cart.getCustomer() != null ? cart.getCustomer().getPhone() : null)
                .itemCount(items.size())
                .totalQuantity(totalQuantity)
                .subtotal(subtotal)
                .updatedAt(cart.getUpdatedAt())
                .build();
    }
}

