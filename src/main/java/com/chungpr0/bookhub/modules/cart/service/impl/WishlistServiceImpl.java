package com.chungpr0.bookhub.modules.cart.service.impl;

import com.chungpr0.bookhub.common.dto.PageMeta;
import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.common.util.DateTimeUtils;
import com.chungpr0.bookhub.modules.cart.dto.request.WishlistFilter;
import com.chungpr0.bookhub.modules.cart.dto.response.WishlistActionResponse;
import com.chungpr0.bookhub.modules.cart.dto.response.WishlistItemResponse;
import com.chungpr0.bookhub.modules.cart.mapper.CartMapper;
import com.chungpr0.bookhub.modules.cart.repository.specification.WishlistSpecification;
import com.chungpr0.bookhub.modules.cart.service.WishlistService;
import com.chungpr0.bookhub.modules.catalog.entity.Book;
import com.chungpr0.bookhub.modules.catalog.entity.Wishlist;
import com.chungpr0.bookhub.modules.catalog.entity.WishlistId;
import com.chungpr0.bookhub.modules.catalog.repository.BookRepository;
import com.chungpr0.bookhub.modules.catalog.repository.WishlistRepository;
import com.chungpr0.bookhub.modules.user.entity.Customer;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private static final int MAX_WISHLIST_ITEMS = 200;

    private final WishlistRepository wishlistRepository;
    private final CustomerRepository customerRepository;
    private final BookRepository bookRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WishlistItemResponse> getWishlist(Long accountId, WishlistFilter filter, Pageable pageable) {
        Customer customer = getCustomerByAccountId(accountId);

        Specification<Wishlist> spec = WishlistSpecification.filter(customer.getId(), filter);
        Page<Wishlist> page = wishlistRepository.findAll(spec, pageable);

        List<WishlistItemResponse> items = page.getContent().stream()
                .map(cartMapper::toWishlistItemResponse)
                .toList();

        PageMeta meta = PageMeta.builder()
                .number(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();

        return PageResponse.of(items, meta);
    }

    @Override
    @Transactional
    public WishlistActionResponse addToWishlist(Long accountId, Long bookId) {
        Customer customer = getCustomerByAccountId(accountId);

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_FOUND));

        boolean alreadyExists = wishlistRepository.existsByIdCustomerIdAndIdBookId(customer.getId(), book.getId());
        if (alreadyExists) {
            return WishlistActionResponse.builder()
                    .bookId(book.getId())
                    .isInWishlist(true)
                    .build();
        }

        long currentCount = wishlistRepository.countByIdCustomerId(customer.getId());
        if (currentCount >= MAX_WISHLIST_ITEMS) {
            throw new AppException(ErrorCode.WISHLIST_LIMIT_EXCEEDED);
        }

        Wishlist wishlist = Wishlist.builder()
                .id(new WishlistId(customer.getId(), book.getId()))
                .customer(customer)
                .book(book)
                .createdAt(DateTimeUtils.nowVietnam())
                .build();

        wishlistRepository.save(wishlist);

        return WishlistActionResponse.builder()
                .bookId(book.getId())
                .isInWishlist(true)
                .build();
    }

    @Override
    @Transactional
    public WishlistActionResponse removeFromWishlist(Long accountId, Long bookId) {
        Customer customer = getCustomerByAccountId(accountId);

        wishlistRepository.deleteByIdCustomerIdAndIdBookId(customer.getId(), bookId);

        return WishlistActionResponse.builder()
                .bookId(bookId)
                .isInWishlist(false)
                .build();
    }

    private Customer getCustomerByAccountId(Long accountId) {
        return customerRepository.findByAccountId(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));
    }
}

