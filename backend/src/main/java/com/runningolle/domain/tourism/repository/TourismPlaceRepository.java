package com.runningolle.domain.tourism.repository;

import com.runningolle.domain.tourism.entity.TourismPlace;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TourismPlaceRepository extends JpaRepository<TourismPlace, UUID> {

    Optional<TourismPlace> findByContentId(String contentId);
}
