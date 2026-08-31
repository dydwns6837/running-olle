package com.runningolle.domain.user.dto;

import com.runningolle.domain.user.entity.Theme;
import java.util.UUID;

public record ThemeResponse(
        UUID id,
        String code,
        String name
) {

    public static ThemeResponse from(Theme theme) {
        return new ThemeResponse(
                theme.getId(),
                theme.getCode(),
                theme.getName()
        );
    }
}
