package com.meta12.SS8911.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final Cloudinary cloudinary;

    // 영상, 이미지 둘 다 이 메서드로 처리 가능
    public String uploadFile(MultipartFile file) throws IOException {
        String resourceType = file.getContentType() != null
                && file.getContentType().startsWith("video") ? "video" : "image";

        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap("resource_type", resourceType)
        );

        // DB에 저장할 URL
        return uploadResult.get("secure_url").toString();
    }
}