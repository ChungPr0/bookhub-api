package com.chungpr0.bookhub.modules.cart.service;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.modules.cart.dto.request.WishlistFilter;
import com.chungpr0.bookhub.modules.cart.dto.response.WishlistActionResponse;
import com.chungpr0.bookhub.modules.cart.dto.response.WishlistItemResponse;
import org.springframework.data.domain.Pageable;

public interface WishlistService {

    PageResponse<WishlistItemResponse> getWishlist(Long accountId, WishlistFilter filter, Pageable pageable);

    WishlistActionResponse addToWishlist(Long accountId, Long bookId);

    WishlistActionResponse removeFromWishlist(Long accountId, Long bookId);
}

