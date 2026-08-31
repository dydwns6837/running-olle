package com.runningolle.domain.community.repository;

import com.runningolle.domain.community.entity.FeedLike;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedLikeRepository extends JpaRepository<FeedLike, UUID> {
    Optional<FeedLike> findByFeedPostIdAndUserId(UUID feedPostId, UUID userId);
    long countByFeedPostId(UUID feedPostId);
    boolean existsByFeedPostIdAndUserId(UUID feedPostId, UUID userId);
}
