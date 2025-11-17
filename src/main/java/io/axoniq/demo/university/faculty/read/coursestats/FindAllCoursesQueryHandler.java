package io.axoniq.demo.university.faculty.read.coursestats;

import org.axonframework.messaging.queryhandling.annotation.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public record FindAllCoursesQueryHandler(
  CourseStatsRepository repository
) {

  @QueryHandler
  List<CoursesQueryResult> handle(FindAllCourses query) {
    return repository.findAll().stream().map(CoursesQueryResult::new).toList();
  }
}
