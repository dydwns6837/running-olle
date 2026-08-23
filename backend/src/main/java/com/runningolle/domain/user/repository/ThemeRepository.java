package com.runningolle.domain.user.repository;

import com.runningolle.domain.user.entity.Theme;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThemeRepository extends JpaRepository<Theme, UUID> {
}
