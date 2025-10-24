package io.axoniq.demo.university.faculty.read.coursestats;

import io.axoniq.demo.university.shared.ids.CourseId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
class InMemoryCourseStatsRepository implements CourseStatsRepository {

    private final ConcurrentHashMap<CourseId, CoursesStatsReadModel> stats = new ConcurrentHashMap<>();

    @Override
    public CoursesStatsReadModel save(CoursesStatsReadModel stats) {
        this.stats.put(stats.courseId(), stats);
        return stats;
    }

    @Override
    public Optional<CoursesStatsReadModel> findById(CourseId courseId) {
        return Optional.ofNullable(stats.get(courseId));
    }

    @Override
    public List<CoursesStatsReadModel> findAll() {
        return stats.values().stream().toList();
    }

}
