package com.runningolle.domain.meetup.controller;

import com.runningolle.domain.meetup.dto.MeetupCreateRequest;
import com.runningolle.domain.meetup.dto.MeetupResponse;
import com.runningolle.domain.meetup.service.MeetupService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/community/meetups")
@RequiredArgsConstructor
public class MeetupController {

    private final MeetupService meetupService;

    @GetMapping
    public ResponseEntity<List<MeetupResponse>> getMeetups(Authentication authentication) {
        return ResponseEntity.ok(meetupService.getMeetups(UUID.fromString(authentication.getName())));
    }

    @GetMapping("/{meetupId}")
    public ResponseEntity<MeetupResponse> getMeetup(Authentication authentication, @PathVariable UUID meetupId) {
        return ResponseEntity.ok(meetupService.getMeetup(UUID.fromString(authentication.getName()), meetupId));
    }

    @PostMapping
    public ResponseEntity<MeetupResponse> createMeetup(
            Authentication authentication,
            @Valid @RequestBody MeetupCreateRequest request
    ) {
        MeetupResponse response = meetupService.createMeetup(UUID.fromString(authentication.getName()), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{meetupId}")
    public ResponseEntity<MeetupResponse> updateMeetup(
            Authentication authentication,
            @PathVariable UUID meetupId,
            @Valid @RequestBody MeetupCreateRequest request
    ) {
        return ResponseEntity.ok(
                meetupService.updateMeetup(UUID.fromString(authentication.getName()), meetupId, request)
        );
    }

    @PostMapping("/{meetupId}/join")
    public ResponseEntity<MeetupResponse> joinMeetup(Authentication authentication, @PathVariable UUID meetupId) {
        return ResponseEntity.ok(meetupService.joinMeetup(UUID.fromString(authentication.getName()), meetupId));
    }

    @PostMapping("/{meetupId}/participants/{participantId}/accept")
    public ResponseEntity<MeetupResponse> acceptParticipant(
            Authentication authentication,
            @PathVariable UUID meetupId,
            @PathVariable UUID participantId
    ) {
        return ResponseEntity.ok(
                meetupService.respondToParticipant(UUID.fromString(authentication.getName()), meetupId, participantId, true)
        );
    }

    @PostMapping("/{meetupId}/participants/{participantId}/reject")
    public ResponseEntity<MeetupResponse> rejectParticipant(
            Authentication authentication,
            @PathVariable UUID meetupId,
            @PathVariable UUID participantId
    ) {
        return ResponseEntity.ok(
                meetupService.respondToParticipant(UUID.fromString(authentication.getName()), meetupId, participantId, false)
        );
    }

    @DeleteMapping("/{meetupId}")
    public ResponseEntity<Void> deleteMeetup(Authentication authentication, @PathVariable UUID meetupId) {
        meetupService.deleteMeetup(UUID.fromString(authentication.getName()), meetupId);
        return ResponseEntity.noContent().build();
    }
}
