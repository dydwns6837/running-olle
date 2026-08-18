package com.runningolle.domain.community.repository;

import com.runningolle.domain.community.entity.FeedPost;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FeedPostRepository extends JpaRepository<FeedPost, UUID> {

    @EntityGraph(attributePaths = {"user", "course", "runningRecord"})
    @Query("""
            select fp
            from FeedPost fp
            where fp.isDeleted = false
              and fp.region = :region
              and fp.createdAt >= :since
            order by fp.createdAt desc
            """)
    List<FeedPost> findRecentVisibleByRegion(String region, LocalDateTime since);

    @EntityGraph(attributePaths = {"user", "course", "runningRecord"})
    Optional<FeedPost> findByIdAndIsDeletedFalse(UUID id);
}
