package com.runningolle.domain.community.repository;

import com.runningolle.domain.community.entity.FeedComment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedCommentRepository extends JpaRepository<FeedComment, UUID> {

    @EntityGraph(attributePaths = {"user"})
    List<FeedComment> findByFeedPostIdAndIsDeletedFalseOrderByCreatedAtAsc(UUID feedPostId);

    long countByFeedPostIdAndIsDeletedFalse(UUID feedPostId);
}
