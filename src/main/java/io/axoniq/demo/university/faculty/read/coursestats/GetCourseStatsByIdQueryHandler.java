package io.axoniq.demo.university.faculty.read.coursestats;

import org.axonframework.queryhandling.annotations.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public record GetCourseStatsByIdQueryHandler(
        CourseStatsRepository repository
) {

    @QueryHandler
    GetCourseStatsById.Result handle(GetCourseStatsById query) {
        var stats = repository.findByIdOrThrow(query.courseId());
        return new GetCourseStatsById.Result(stats);
    }

    @QueryHandler
    List<GetCourseStatsById.Result> handle(FindAll query) {
       return repository.findAll().stream().map(GetCourseStatsById.Result::new).toList();
    }
}
