package io.axoniq.demo.university.faculty.read.coursestats;

import io.axoniq.demo.university.faculty.events.*;
import org.axonframework.eventhandling.annotations.EventHandler;
import org.axonframework.eventhandling.annotations.SequencingPolicy;
import org.axonframework.eventhandling.sequencing.PropertySequencingPolicy;
import org.axonframework.messaging.unitofwork.ProcessingContext;
import org.axonframework.queryhandling.QueryUpdateEmitter;

@SequencingPolicy(type = PropertySequencingPolicy.class, parameters = {"courseId"})
class CoursesStatsProjection {

    private final CourseStatsRepository repository;

    public CoursesStatsProjection(CourseStatsRepository repository) {
        this.repository = repository;
    }

    @EventHandler
    void handle(CourseCreated event, ProcessingContext processingContext) {
        CoursesStatsReadModel readModel = new CoursesStatsReadModel(
                event.courseId(),
                event.name(),
                event.capacity(),
                0
        );
        repository.save(readModel);
        QueryUpdateEmitter.forContext(processingContext)
                .emit(GetCourseStatsById.class, query -> query.courseId().equals(event.courseId()), new GetCourseStatsById.Result(readModel));
    }

    @EventHandler
    void handle(CourseRenamed event) {
        CoursesStatsReadModel readModel = repository.findByIdOrThrow(event.courseId());
        var updatedReadModel = readModel.name(event.name());
        repository.save(updatedReadModel);
    }

    @EventHandler
    void handle(CourseCapacityChanged event) {
        CoursesStatsReadModel readModel = repository.findByIdOrThrow(event.courseId());
        var updatedReadModel = readModel.capacity(event.capacity());
        repository.save(updatedReadModel);
    }

    @EventHandler
    void handle(StudentSubscribedToCourse event) {
        CoursesStatsReadModel readModel = repository.findByIdOrThrow(event.courseId());
        var updatedReadModel = readModel.subscribedStudents(readModel.subscribedStudents() + 1);
        repository.save(updatedReadModel);
    }

    @EventHandler
    void handle(StudentUnsubscribedFromCourse event) {
        CoursesStatsReadModel readModel = repository.findByIdOrThrow(event.courseId());
        var updatedReadModel = readModel.subscribedStudents(readModel.subscribedStudents() - 1);
        repository.save(updatedReadModel);
    }

}
