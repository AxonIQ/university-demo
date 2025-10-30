package io.axoniq.demo.university.faculty.read.coursestats;

import io.axoniq.demo.university.faculty.events.*;
import org.axonframework.eventhandling.annotations.EventHandler;
import org.axonframework.eventhandling.annotations.SequencingPolicy;
import org.axonframework.eventhandling.sequencing.PropertySequencingPolicy;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@SequencingPolicy(type = PropertySequencingPolicy.class, parameters = {"courseId"})
class CoursesStatsProjection {

  private static final Logger logger = LoggerFactory.getLogger(CoursesStatsProjection.class);
  private final CourseStatsRepository repository;

  public CoursesStatsProjection(CourseStatsRepository repository) {
    this.repository = repository;
  }

  @EventHandler
  void handle(CourseCreated event, QueryUpdateEmitter emitter) {
    CoursesStatsReadModel readModel = new CoursesStatsReadModel(
      event.courseId(),
      event.name(),
      event.capacity(),
      0
    );
    repository.save(readModel);
    emitUpdate(emitter, readModel);
  }

  @EventHandler
  void handle(CourseRenamed event, QueryUpdateEmitter emitter) {
    CoursesStatsReadModel readModel = repository.findByIdOrThrow(event.courseId());
    var updatedReadModel = readModel.name(event.name());
    repository.save(updatedReadModel);
    emitUpdate(emitter, updatedReadModel);
  }

  @EventHandler
  void handle(CourseCapacityChanged event, QueryUpdateEmitter emitter) {
    CoursesStatsReadModel readModel = repository.findByIdOrThrow(event.courseId());
    var updatedReadModel = readModel.capacity(event.capacity());
    repository.save(updatedReadModel);
    emitUpdate(emitter, updatedReadModel);
  }

  @EventHandler
  void handle(StudentSubscribedToCourse event, QueryUpdateEmitter emitter) {
    CoursesStatsReadModel readModel = repository.findByIdOrThrow(event.courseId());
    var updatedReadModel = readModel.subscribedStudents(readModel.subscribedStudents() + 1);
    repository.save(updatedReadModel);
    emitUpdate(emitter, updatedReadModel);
  }

  @EventHandler
  void handle(StudentUnsubscribedFromCourse event, QueryUpdateEmitter emitter) {
    CoursesStatsReadModel readModel = repository.findByIdOrThrow(event.courseId());
    var updatedReadModel = readModel.subscribedStudents(readModel.subscribedStudents() - 1);
    repository.save(updatedReadModel);
    emitUpdate(emitter, updatedReadModel);
  }

  /**
   * Emits an update for subscription queries when course stats change.
   * This should be called by the projection when the read model is updated.
   */
  public void emitUpdate(QueryUpdateEmitter emitter, CoursesStatsReadModel updatedStats) {
    logger.info("Emitting updated courses stats for {}", updatedStats.courseId());
    emitter.emit(
      GetCourseStatsById.class,
      query -> updatedStats.courseId().equals(query.courseId()),
      new CoursesQueryResult(updatedStats)
    );
    emitter.emit(FindAllCourses.class,
      q -> true,
      new CoursesQueryResult(updatedStats)
    );
  }
}
