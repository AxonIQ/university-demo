package io.axoniq.demo.university.faculty.read.coursestats;

import org.axonframework.messaging.queryhandling.annotation.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public record GetCourseStatsByIdQueryHandler(
        CourseStatsRepository repository
) {

  @QueryHandler
  CoursesQueryResult handle(GetCourseStatsById query) {
    var stats = repository.findByIdOrThrow(query.courseId());
    return new CoursesQueryResult(stats);
  }
}
