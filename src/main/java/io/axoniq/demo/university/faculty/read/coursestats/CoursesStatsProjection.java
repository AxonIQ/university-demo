package io.axoniq.demo.university.faculty.read.coursestats;

import io.axoniq.demo.university.faculty.events.*;
import org.axonframework.eventhandling.annotations.EventHandler;
import org.axonframework.eventhandling.annotations.SequencingPolicy;
import org.axonframework.eventhandling.sequencing.PropertySequencingPolicy;
import org.axonframework.messaging.unitofwork.ProcessingContext;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.stereotype.Component;

@Component
@SequencingPolicy(type = PropertySequencingPolicy.class, parameters = {"courseId"})
class CoursesStatsProjection {

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
    void handle(CourseRenamed event,  QueryUpdateEmitter emitter) {
        CoursesStatsReadModel readModel = repository.findByIdOrThrow(event.courseId());
        var updatedReadModel = readModel.name(event.name());
        repository.save(updatedReadModel);
        emitUpdate(emitter, readModel);
    }

    @EventHandler
    void handle(CourseCapacityChanged event,  QueryUpdateEmitter emitter) {
        CoursesStatsReadModel readModel = repository.findByIdOrThrow(event.courseId());
        var updatedReadModel = readModel.capacity(event.capacity());
        repository.save(updatedReadModel);
        emitUpdate(emitter, readModel);
    }

    @EventHandler
    void handle(StudentSubscribedToCourse event,  QueryUpdateEmitter emitter) {
        CoursesStatsReadModel readModel = repository.findByIdOrThrow(event.courseId());
        var updatedReadModel = readModel.subscribedStudents(readModel.subscribedStudents() + 1);
        repository.save(updatedReadModel);
        emitUpdate(emitter, readModel);
    }

    @EventHandler
    void handle(StudentUnsubscribedFromCourse event,  QueryUpdateEmitter emitter) {
        CoursesStatsReadModel readModel = repository.findByIdOrThrow(event.courseId());
        var updatedReadModel = readModel.subscribedStudents(readModel.subscribedStudents() - 1);
        repository.save(updatedReadModel);
        emitUpdate(emitter, readModel);
    }

    /**
     * Emits an update for subscription queries when course stats change.
     * This should be called by the projection when the read model is updated.
     */
    public void emitUpdate(QueryUpdateEmitter emitter, CoursesStatsReadModel updatedStats) {
        emitter.emit(
                GetCourseStatsById.class,
                query -> true,
                new GetCourseStatsById.Result(updatedStats)
        );
    }
}
