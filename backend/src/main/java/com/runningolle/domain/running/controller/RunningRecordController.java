package com.runningolle.domain.running.controller;

import com.runningolle.domain.running.dto.CreateRunningRecordRequest;
import com.runningolle.domain.running.dto.CreateRunningRecordResponse;
import com.runningolle.domain.running.service.RunningRecordService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/running-records")
@RequiredArgsConstructor
public class RunningRecordController {

    private final RunningRecordService runningRecordService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateRunningRecordResponse create(
            Authentication authentication,
            @Valid @RequestBody CreateRunningRecordRequest request
    ) {
        UUID id = runningRecordService.createFreeRun(UUID.fromString(authentication.getName()), request);
        return new CreateRunningRecordResponse(id);
    }
}
