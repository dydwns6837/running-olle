package com.runningolle.domain.community.repository;

import com.runningolle.domain.community.entity.FeedPostImage;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedPostImageRepository extends JpaRepository<FeedPostImage, UUID> {
    List<FeedPostImage> findByFeedPostIdOrderByOrderIndexAsc(UUID feedPostId);
    void deleteByFeedPostId(UUID feedPostId);
}
