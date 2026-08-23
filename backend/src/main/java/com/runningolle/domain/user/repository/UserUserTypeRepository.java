package com.runningolle.domain.user.repository;

import com.runningolle.domain.user.entity.UserUserType;
import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserUserTypeRepository extends JpaRepository<UserUserType, UUID> {
    void deleteAllByUserId(UUID userId);

    @EntityGraph(attributePaths = "userType")
    List<UserUserType> findAllByUserId(UUID userId);
}
