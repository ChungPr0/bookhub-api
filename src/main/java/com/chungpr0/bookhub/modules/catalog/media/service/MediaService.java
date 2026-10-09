package com.chungpr0.bookhub.modules.catalog.media.service;

import com.chungpr0.bookhub.modules.catalog.media.dto.MediaBatchUploadResponse;
import com.chungpr0.bookhub.modules.catalog.media.dto.MediaUploadResponse;
import com.chungpr0.bookhub.modules.catalog.media.enums.MediaFolder;
import com.chungpr0.bookhub.security.UserPrincipal;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MediaService {

    MediaUploadResponse uploadSingle(MultipartFile file, MediaFolder folder, UserPrincipal principal);

    MediaBatchUploadResponse uploadBatch(List<MultipartFile> files, MediaFolder folder, UserPrincipal principal);
}

