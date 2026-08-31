package com.runningolle.domain.community.dto;

import java.util.List;

public record ImageUploadResponse(
        List<String> imageUrls
) {
}
