package com.runningolle.domain.community.dto;

import com.runningolle.domain.community.enums.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record FeedPostCreateRequest(
        UUID runningRecordId,
        UUID courseId,
        @NotBlank @Size(max = 2000) String content,
        @NotNull Visibility visibility,
        @NotBlank @Size(max = 20) String region,
        boolean photoTagged,
        @Size(max = 10) List<@NotBlank String> imageUrls
) {
}
