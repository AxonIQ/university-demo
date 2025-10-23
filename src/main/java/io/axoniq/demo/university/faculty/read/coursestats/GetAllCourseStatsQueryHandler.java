package io.axoniq.demo.university.faculty.read.coursestats;

import org.axonframework.queryhandling.annotations.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public record GetAllCourseStatsQueryHandler(
        CourseStatsRepository repository
) {

    @QueryHandler
    GetAllCourseStats.Result handle(GetAllCourseStats query) {
        List<CoursesStatsReadModel> allStats = repository.findAll();
        return new GetAllCourseStats.Result(allStats);
    }

}