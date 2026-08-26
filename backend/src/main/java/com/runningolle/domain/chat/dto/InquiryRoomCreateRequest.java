package com.runningolle.domain.chat.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record InquiryRoomCreateRequest(
        @NotNull UUID meetupId
) {
}
