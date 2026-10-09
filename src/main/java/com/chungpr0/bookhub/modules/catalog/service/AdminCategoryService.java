package com.chungpr0.bookhub.modules.catalog.service;

import com.chungpr0.bookhub.modules.catalog.dto.request.CreateCategoryRequest;
import com.chungpr0.bookhub.modules.catalog.dto.request.UpdateCategoryRequest;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminCategoryResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminCategoryTreeResponse;

import java.util.List;

public interface AdminCategoryService {

    List<AdminCategoryTreeResponse> getAdminCategoryTree();

    AdminCategoryResponse createCategory(CreateCategoryRequest request);

    AdminCategoryResponse updateCategory(Long id, UpdateCategoryRequest request);

    void deleteCategory(Long id);
}

