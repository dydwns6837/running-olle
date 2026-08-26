package com.runningolle.domain.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FeedCommentCreateRequest(
        @NotBlank @Size(max = 500) String content
) {
}
