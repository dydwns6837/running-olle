package com.runningolle.domain.community.controller;

import com.runningolle.domain.community.dto.FeedCommentCreateRequest;
import com.runningolle.domain.community.dto.FeedCommentResponse;
import com.runningolle.domain.community.dto.FeedLikeToggleResponse;
import com.runningolle.domain.community.dto.FeedPostCreateRequest;
import com.runningolle.domain.community.dto.FeedPostResponse;
import com.runningolle.domain.community.dto.FeedSelectionOptionResponse;
import com.runningolle.domain.community.dto.FeedPostUpdateRequest;
import com.runningolle.domain.community.service.FeedService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/community/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @GetMapping
    public ResponseEntity<List<FeedPostResponse>> getFeed(Authentication authentication) {
        return ResponseEntity.ok(feedService.getFeed(UUID.fromString(authentication.getName())));
    }

    @GetMapping("/{feedPostId}")
    public ResponseEntity<FeedPostResponse> getPost(Authentication authentication, @PathVariable UUID feedPostId) {
        return ResponseEntity.ok(feedService.getPost(UUID.fromString(authentication.getName()), feedPostId));
    }

    @GetMapping("/options/running-records")
    public ResponseEntity<List<FeedSelectionOptionResponse>> getRunningRecordOptions(Authentication authentication) {
        return ResponseEntity.ok(feedService.getMyRunningRecordOptions(UUID.fromString(authentication.getName())));
    }

    @GetMapping("/options/courses")
    public ResponseEntity<List<FeedSelectionOptionResponse>> getCourseOptions() {
        return ResponseEntity.ok(feedService.getCourseOptions());
    }

    @PostMapping
    public ResponseEntity<FeedPostResponse> createPost(
            Authentication authentication,
            @Valid @RequestBody FeedPostCreateRequest request
    ) {
        FeedPostResponse response = feedService.createPost(UUID.fromString(authentication.getName()), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{feedPostId}")
    public ResponseEntity<FeedPostResponse> updatePost(
            Authentication authentication,
            @PathVariable UUID feedPostId,
            @Valid @RequestBody FeedPostUpdateRequest request
    ) {
        return ResponseEntity.ok(feedService.updatePost(UUID.fromString(authentication.getName()), feedPostId, request));
    }

    @PostMapping("/{feedPostId}/likes")
    public ResponseEntity<FeedLikeToggleResponse> toggleLike(
            Authentication authentication,
            @PathVariable UUID feedPostId
    ) {
        return ResponseEntity.ok(feedService.toggleLike(UUID.fromString(authentication.getName()), feedPostId));
    }

    @PostMapping("/{feedPostId}/comments")
    public ResponseEntity<FeedCommentResponse> addComment(
            Authentication authentication,
            @PathVariable UUID feedPostId,
            @Valid @RequestBody FeedCommentCreateRequest request
    ) {
        FeedCommentResponse response = feedService.addComment(
                UUID.fromString(authentication.getName()),
                feedPostId,
                request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{feedPostId}")
    public ResponseEntity<Void> deletePost(Authentication authentication, @PathVariable UUID feedPostId) {
        feedService.deletePost(UUID.fromString(authentication.getName()), feedPostId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(Authentication authentication, @PathVariable UUID commentId) {
        feedService.deleteComment(UUID.fromString(authentication.getName()), commentId);
        return ResponseEntity.noContent().build();
    }
}
