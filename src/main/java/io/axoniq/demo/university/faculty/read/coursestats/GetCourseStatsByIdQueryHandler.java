package io.axoniq.demo.university.faculty.read.coursestats;

import org.axonframework.messaging.unitofwork.ProcessingContext;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.axonframework.queryhandling.annotations.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public record GetCourseStatsByIdQueryHandler(
        CourseStatsRepository repository
) {

    @QueryHandler
    GetCourseStatsById.Result handle(GetCourseStatsById query) {
        var stats = repository.findByIdOrThrow(query.courseId());
        return new GetCourseStatsById.Result(stats);
    }

}
