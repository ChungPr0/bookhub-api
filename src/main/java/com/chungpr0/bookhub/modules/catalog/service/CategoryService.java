package com.chungpr0.bookhub.modules.catalog.service;

import com.chungpr0.bookhub.modules.catalog.dto.response.CategoryDetailResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.CategoryTreeResponse;

import java.util.List;

public interface CategoryService {

    List<CategoryTreeResponse> getCategoryTree();

    CategoryDetailResponse getCategoryBySlug(String slug);
}

