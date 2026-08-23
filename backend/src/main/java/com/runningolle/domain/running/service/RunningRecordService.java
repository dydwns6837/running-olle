package com.runningolle.domain.running.service;

import com.runningolle.domain.running.dto.CreateRunningRecordRequest;
import com.runningolle.domain.running.entity.RunningRecord;
import com.runningolle.domain.running.repository.RunningRecordRepository;
import com.runningolle.domain.user.entity.User;
import com.runningolle.domain.user.enums.AccountStatus;
import com.runningolle.domain.user.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class RunningRecordService {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    private final RunningRecordRepository runningRecordRepository;
    private final UserRepository userRepository;

    @Transactional
    public UUID createFreeRun(UUID userId, CreateRunningRecordRequest request) {
        User user = userRepository.findById(userId)
                .filter(found -> found.getAccountStatus() == AccountStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (request.endedAt().isBefore(request.startedAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "종료 시각은 시작 시각보다 빠를 수 없습니다.");
        }

        RunningRecord record = RunningRecord.createFreeRun(
                user,
                toLineString(request.route()),
                BigDecimal.valueOf(request.totalDistanceMeters() / 1_000).setScale(2, RoundingMode.HALF_UP),
                request.totalDurationSeconds(),
                decimalOrNull(request.averagePace()),
                BigDecimal.valueOf(request.calories()).setScale(2, RoundingMode.HALF_UP),
                LocalDateTime.ofInstant(request.startedAt().toInstant(), ZoneOffset.UTC),
                LocalDateTime.ofInstant(request.endedAt().toInstant(), ZoneOffset.UTC)
        );
        return runningRecordRepository.save(record).getId();
    }

    private LineString toLineString(List<CreateRunningRecordRequest.RoutePoint> points) {
        List<Coordinate> coordinates = new ArrayList<>(points.stream()
                .map(point -> new Coordinate(point.longitude(), point.latitude()))
                .toList());
        if (coordinates.size() == 1) coordinates.add(new Coordinate(coordinates.get(0)));
        return GEOMETRY_FACTORY.createLineString(coordinates.toArray(Coordinate[]::new));
    }

    private BigDecimal decimalOrNull(Double value) {
        return value == null ? null : BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}
