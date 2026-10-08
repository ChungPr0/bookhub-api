package com.chungpr0.bookhub.modules.cart.service.impl;

import com.chungpr0.bookhub.common.dto.PageMeta;
import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.common.enums.BookStatus;
import com.chungpr0.bookhub.common.enums.CartMergeResult;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.common.util.DateTimeUtils;
import com.chungpr0.bookhub.modules.cart.dto.request.AddToCartRequest;
import com.chungpr0.bookhub.modules.cart.dto.request.CartMergeItemRequest;
import com.chungpr0.bookhub.modules.cart.dto.request.CartMergeRequest;
import com.chungpr0.bookhub.modules.cart.dto.request.CartSearchFilter;
import com.chungpr0.bookhub.modules.cart.dto.request.UpdateCartItemQuantityRequest;
import com.chungpr0.bookhub.modules.cart.dto.response.AdminCartResponse;
import com.chungpr0.bookhub.modules.cart.dto.response.CartMergeReportItem;
import com.chungpr0.bookhub.modules.cart.dto.response.CartMergeResponse;
import com.chungpr0.bookhub.modules.cart.dto.response.CartResponse;
import com.chungpr0.bookhub.modules.cart.entity.Cart;
import com.chungpr0.bookhub.modules.cart.entity.CartItem;
import com.chungpr0.bookhub.modules.cart.mapper.CartMapper;
import com.chungpr0.bookhub.modules.cart.repository.CartItemRepository;
import com.chungpr0.bookhub.modules.cart.repository.CartRepository;
import com.chungpr0.bookhub.modules.cart.repository.specification.CartSpecification;
import com.chungpr0.bookhub.modules.cart.service.CartService;
import com.chungpr0.bookhub.modules.catalog.entity.Book;
import com.chungpr0.bookhub.modules.catalog.repository.BookRepository;
import com.chungpr0.bookhub.modules.user.entity.Customer;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private static final int MAX_DISTINCT_ITEMS = 50;
    private static final int MAX_ITEM_QUANTITY = 99;

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CustomerRepository customerRepository;
    private final BookRepository bookRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional
    public CartResponse getCart(Long accountId) {
        Customer customer = getCustomerByAccountId(accountId);
        Cart cart = getOrCreateCart(customer);
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        return cartMapper.toCartResponse(cart, items);
    }

    @Override
    @Transactional
    public CartResponse addToCart(Long accountId, AddToCartRequest request) {
        Customer customer = getCustomerByAccountId(accountId);
        Cart cart = getOrCreateCart(customer);

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_FOUND));

        if (book.getStatus() == BookStatus.INACTIVE) {
            throw new AppException(ErrorCode.BOOK_NOT_AVAILABLE);
        }

        if (book.getStockQuantity() <= 0) {
            throw new AppException(ErrorCode.OUT_OF_STOCK);
        }

        Optional<CartItem> existingItemOpt = cartItemRepository.findByCartIdAndBookId(cart.getId(), book.getId());

        int targetQuantity;
        CartItem cartItem;

        if (existingItemOpt.isPresent()) {
            cartItem = existingItemOpt.get();
            targetQuantity = cartItem.getQuantity() + request.getQuantity();
        } else {
            int currentDistinctCount = cartItemRepository.countByCartId(cart.getId());
            if (currentDistinctCount >= MAX_DISTINCT_ITEMS) {
                throw new AppException(ErrorCode.CART_LIMIT_EXCEEDED);
            }
            targetQuantity = request.getQuantity();
            cartItem = CartItem.builder()
                    .cart(cart)
                    .book(book)
                    .quantity(targetQuantity)
                    .build();
        }

        if (targetQuantity > MAX_ITEM_QUANTITY) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Số lượng mua mỗi đầu sách phải từ 1 đến 99");
        }

        if (targetQuantity > book.getStockQuantity()) {
            Map<String, Object> details = Map.of(
                    "items", List.of(
                            Map.of(
                                    "bookId", book.getId(),
                                    "title", book.getTitle(),
                                    "requested", targetQuantity,
                                    "available", book.getStockQuantity()
                            )
                    )
            );
            throw new AppException(ErrorCode.INSUFFICIENT_STOCK,
                    String.format("Số lượng yêu cầu (%d) vượt quá tồn kho khả dụng của sản phẩm (%d)",
                            targetQuantity, book.getStockQuantity()), details);
        }

        cartItem.setQuantity(targetQuantity);
        cartItemRepository.save(cartItem);

        cart.onUpdate();
        cartRepository.save(cart);

        List<CartItem> updatedItems = cartItemRepository.findByCartId(cart.getId());
        return cartMapper.toCartResponse(cart, updatedItems);
    }

    @Override
    @Transactional
    public CartResponse updateItemQuantity(Long accountId, Long bookId, UpdateCartItemQuantityRequest request) {
        Customer customer = getCustomerByAccountId(accountId);
        Cart cart = getOrCreateCart(customer);

        CartItem cartItem = cartItemRepository.findByCartIdAndBookId(cart.getId(), bookId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        Book book = cartItem.getBook();
        int requestedQuantity = request.getQuantity();

        if (requestedQuantity > book.getStockQuantity()) {
            Map<String, Object> details = Map.of(
                    "items", List.of(
                            Map.of(
                                    "bookId", book.getId(),
                                    "title", book.getTitle(),
                                    "requested", requestedQuantity,
                                    "available", book.getStockQuantity()
                            )
                    )
            );
            throw new AppException(ErrorCode.INSUFFICIENT_STOCK,
                    String.format("Số lượng yêu cầu (%d) vượt quá tồn kho khả dụng của sản phẩm (%d)",
                            requestedQuantity, book.getStockQuantity()), details);
        }

        cartItem.setQuantity(requestedQuantity);
        cartItemRepository.save(cartItem);

        cart.onUpdate();
        cartRepository.save(cart);

        List<CartItem> updatedItems = cartItemRepository.findByCartId(cart.getId());
        return cartMapper.toCartResponse(cart, updatedItems);
    }

    @Override
    @Transactional
    public CartResponse removeItem(Long accountId, Long bookId) {
        Customer customer = getCustomerByAccountId(accountId);
        Cart cart = getOrCreateCart(customer);

        CartItem cartItem = cartItemRepository.findByCartIdAndBookId(cart.getId(), bookId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        cartItemRepository.delete(cartItem);

        cart.onUpdate();
        cartRepository.save(cart);

        List<CartItem> updatedItems = cartItemRepository.findByCartId(cart.getId());
        return cartMapper.toCartResponse(cart, updatedItems);
    }

    @Override
    @Transactional
    public CartResponse removeItems(Long accountId, List<Long> bookIds) {
        Customer customer = getCustomerByAccountId(accountId);
        Cart cart = getOrCreateCart(customer);

        if (bookIds != null && !bookIds.isEmpty()) {
            cartItemRepository.deleteByCartIdAndBookIdIn(cart.getId(), bookIds);
            cart.onUpdate();
            cartRepository.save(cart);
        }

        List<CartItem> updatedItems = cartItemRepository.findByCartId(cart.getId());
        return cartMapper.toCartResponse(cart, updatedItems);
    }

    @Override
    @Transactional
    public CartResponse clearCart(Long accountId) {
        Customer customer = getCustomerByAccountId(accountId);
        Cart cart = getOrCreateCart(customer);

        cartItemRepository.deleteAllByCartId(cart.getId());

        cart.onUpdate();
        cartRepository.save(cart);

        return cartMapper.toCartResponse(cart, Collections.emptyList());
    }

    @Override
    @Transactional
    public CartMergeResponse mergeCart(Long accountId, CartMergeRequest request) {
        Customer customer = getCustomerByAccountId(accountId);
        Cart cart = getOrCreateCart(customer);

        List<CartMergeReportItem> report = new ArrayList<>();

        if (request != null && request.getItems() != null) {
            for (CartMergeItemRequest itemReq : request.getItems()) {
                Optional<Book> bookOpt = bookRepository.findById(itemReq.getBookId());
                if (bookOpt.isEmpty()) {
                    report.add(CartMergeReportItem.builder()
                            .bookId(itemReq.getBookId())
                            .requestedQuantity(itemReq.getQuantity())
                            .mergedQuantity(0)
                            .result(CartMergeResult.SKIPPED)
                            .reason("Cuốn sách không tồn tại")
                            .build());
                    continue;
                }

                Book book = bookOpt.get();
                if (book.getStatus() == BookStatus.INACTIVE) {
                    report.add(CartMergeReportItem.builder()
                            .bookId(book.getId())
                            .requestedQuantity(itemReq.getQuantity())
                            .mergedQuantity(0)
                            .result(CartMergeResult.SKIPPED)
                            .reason("Cuốn sách này hiện đã ngừng kinh doanh")
                            .build());
                    continue;
                }

                if (book.getStockQuantity() <= 0) {
                    report.add(CartMergeReportItem.builder()
                            .bookId(book.getId())
                            .requestedQuantity(itemReq.getQuantity())
                            .mergedQuantity(0)
                            .result(CartMergeResult.SKIPPED)
                            .reason("Cuốn sách này hiện đã hết hàng")
                            .build());
                    continue;
                }

                Optional<CartItem> existingItemOpt = cartItemRepository.findByCartIdAndBookId(cart.getId(), book.getId());

                if (existingItemOpt.isPresent()) {
                    CartItem existingItem = existingItemOpt.get();
                    int combinedQuantity = existingItem.getQuantity() + itemReq.getQuantity();
                    int cappedQuantity = Math.min(combinedQuantity, MAX_ITEM_QUANTITY);
                    int finalQuantity = Math.min(cappedQuantity, book.getStockQuantity());

                    existingItem.setQuantity(finalQuantity);
                    cartItemRepository.save(existingItem);

                    if (finalQuantity < combinedQuantity) {
                        report.add(CartMergeReportItem.builder()
                                .bookId(book.getId())
                                .requestedQuantity(itemReq.getQuantity())
                                .mergedQuantity(finalQuantity)
                                .result(CartMergeResult.ADJUSTED)
                                .reason(String.format("Số lượng đã được tự động giảm xuống %d do tồn kho có hạn", finalQuantity))
                                .build());
                    } else {
                        report.add(CartMergeReportItem.builder()
                                .bookId(book.getId())
                                .requestedQuantity(itemReq.getQuantity())
                                .mergedQuantity(finalQuantity)
                                .result(CartMergeResult.MERGED)
                                .reason(null)
                                .build());
                    }
                } else {
                    int distinctCount = cartItemRepository.countByCartId(cart.getId());
                    if (distinctCount >= MAX_DISTINCT_ITEMS) {
                        report.add(CartMergeReportItem.builder()
                                .bookId(book.getId())
                                .requestedQuantity(itemReq.getQuantity())
                                .mergedQuantity(0)
                                .result(CartMergeResult.SKIPPED)
                                .reason("Giỏ hàng đã đạt giới hạn tối đa 50 đầu sách khác nhau")
                                .build());
                        continue;
                    }

                    int cappedQuantity = Math.min(itemReq.getQuantity(), MAX_ITEM_QUANTITY);
                    int finalQuantity = Math.min(cappedQuantity, book.getStockQuantity());

                    CartItem newItem = CartItem.builder()
                            .cart(cart)
                            .book(book)
                            .quantity(finalQuantity)
                            .build();
                    cartItemRepository.save(newItem);

                    if (finalQuantity < itemReq.getQuantity()) {
                        report.add(CartMergeReportItem.builder()
                                .bookId(book.getId())
                                .requestedQuantity(itemReq.getQuantity())
                                .mergedQuantity(finalQuantity)
                                .result(CartMergeResult.ADJUSTED)
                                .reason(String.format("Số lượng đã được tự động giảm xuống %d do tồn kho có hạn", finalQuantity))
                                .build());
                    } else {
                        report.add(CartMergeReportItem.builder()
                                .bookId(book.getId())
                                .requestedQuantity(itemReq.getQuantity())
                                .mergedQuantity(finalQuantity)
                                .result(CartMergeResult.MERGED)
                                .reason(null)
                                .build());
                    }
                }
            }

            cart.onUpdate();
            cartRepository.save(cart);
        }

        List<CartItem> updatedItems = cartItemRepository.findByCartId(cart.getId());
        CartResponse cartResponse = cartMapper.toCartResponse(cart, updatedItems);

        return CartMergeResponse.builder()
                .cart(cartResponse)
                .mergeReport(report)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminCartResponse> searchCarts(CartSearchFilter filter, Pageable pageable) {
        Specification<Cart> spec = CartSpecification.filter(filter);
        Page<Cart> page = cartRepository.findAll(spec, pageable);

        List<AdminCartResponse> items = page.getContent().stream()
                .map(cartMapper::toAdminCartResponse)
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
    @Transactional(readOnly = true)
    public CartResponse getCartById(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy giỏ hàng yêu cầu"));
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        return cartMapper.toCartResponse(cart, items);
    }

    private Customer getCustomerByAccountId(Long accountId) {
        return customerRepository.findByAccountId(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));
    }

    private Cart getOrCreateCart(Customer customer) {
        return cartRepository.findByCustomerId(customer.getId())
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .customer(customer)
                            .updatedAt(DateTimeUtils.nowVietnam())
                            .build();
                    return cartRepository.save(newCart);
                });
    }
}

