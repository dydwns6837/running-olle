package com.runningolle.domain.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatMessageCreateRequest(
        @NotBlank @Size(max = 4000) String content
) {
}
