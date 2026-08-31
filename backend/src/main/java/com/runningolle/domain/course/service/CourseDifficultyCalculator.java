package com.runningolle.domain.course.service;

import com.runningolle.domain.course.enums.Difficulty;

final class CourseDifficultyCalculator {

    private static final double LOW_DIFFICULTY_ELEVATION_GAIN_PER_KM = 10.0;
    private static final double MID_DIFFICULTY_ELEVATION_GAIN_PER_KM = 30.0;

    private CourseDifficultyCalculator() {
    }

    static Difficulty suggest(double distanceKm, double elevationGainM) {
        if (distanceKm <= 0) {
            return Difficulty.LOW;
        }

        double elevationGainPerKm = elevationGainM / distanceKm;
        if (elevationGainPerKm < LOW_DIFFICULTY_ELEVATION_GAIN_PER_KM) {
            return Difficulty.LOW;
        }
        if (elevationGainPerKm < MID_DIFFICULTY_ELEVATION_GAIN_PER_KM) {
            return Difficulty.MID;
        }
        return Difficulty.HIGH;
    }
}
