package com.chungpr0.bookhub.modules.cart.service;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.modules.cart.dto.request.AddToCartRequest;
import com.chungpr0.bookhub.modules.cart.dto.request.CartMergeRequest;
import com.chungpr0.bookhub.modules.cart.dto.request.CartSearchFilter;
import com.chungpr0.bookhub.modules.cart.dto.request.UpdateCartItemQuantityRequest;
import com.chungpr0.bookhub.modules.cart.dto.response.AdminCartResponse;
import com.chungpr0.bookhub.modules.cart.dto.response.CartMergeResponse;
import com.chungpr0.bookhub.modules.cart.dto.response.CartResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CartService {

    CartResponse getCart(Long accountId);

    CartResponse addToCart(Long accountId, AddToCartRequest request);

    CartResponse updateItemQuantity(Long accountId, Long bookId, UpdateCartItemQuantityRequest request);

    CartResponse removeItem(Long accountId, Long bookId);

    CartResponse removeItems(Long accountId, List<Long> bookIds);

    CartResponse clearCart(Long accountId);

    CartMergeResponse mergeCart(Long accountId, CartMergeRequest request);

    PageResponse<AdminCartResponse> searchCarts(CartSearchFilter filter, Pageable pageable);

    CartResponse getCartById(Long cartId);
}

