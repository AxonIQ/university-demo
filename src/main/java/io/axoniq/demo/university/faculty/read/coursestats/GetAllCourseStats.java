package io.axoniq.demo.university.faculty.read.coursestats;

import java.util.List;

public record GetAllCourseStats() {
    public record Result(List<CoursesStatsReadModel> stats) {
    }
}