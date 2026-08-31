package com.runningolle.domain.user.controller;

import com.runningolle.domain.user.dto.ThemeResponse;
import com.runningolle.domain.user.repository.ThemeRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/themes")
public class ThemeController {

    private final ThemeRepository themeRepository;

    @GetMapping
    public List<ThemeResponse> getThemes() {
        return themeRepository.findAll().stream()
                .map(ThemeResponse::from)
                .sorted(Comparator.comparing(ThemeResponse::name))
                .toList();
    }
}
