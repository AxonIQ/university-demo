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

        // Emit update for single course stats subscription
        emitter.emit(
                GetCourseStatsById.class,
                query -> true,
                new GetCourseStatsById.Result(readModel)
        );

        // Emit update for all course stats subscription
        emitter.emit(
                GetAllCourseStats.class,
                query -> true,
                new GetAllCourseStats.Result(repository.findAll())
        );
    }

    @EventHandler
    void handle(CourseRenamed event, ProcessingContext processingContext) {
        CoursesStatsReadModel readModel = repository.findByIdOrThrow(event.courseId());
        var updatedReadModel = readModel.name(event.name());
        repository.save(updatedReadModel);
        emitUpdate(processingContext, updatedReadModel);
    }

    @EventHandler
    void handle(CourseCapacityChanged event, ProcessingContext processingContext) {
        CoursesStatsReadModel readModel = repository.findByIdOrThrow(event.courseId());
        var updatedReadModel = readModel.capacity(event.capacity());
        repository.save(updatedReadModel);
        emitUpdate(processingContext, updatedReadModel);
    }

    @EventHandler
    void handle(StudentSubscribedToCourse event, ProcessingContext processingContext) {
        CoursesStatsReadModel readModel = repository.findByIdOrThrow(event.courseId());
        var updatedReadModel = readModel.subscribedStudents(readModel.subscribedStudents() + 1);
        repository.save(updatedReadModel);
        emitUpdate(processingContext, updatedReadModel);
    }

    @EventHandler
    void handle(StudentUnsubscribedFromCourse event, ProcessingContext processingContext) {
        CoursesStatsReadModel readModel = repository.findByIdOrThrow(event.courseId());
        var updatedReadModel = readModel.subscribedStudents(readModel.subscribedStudents() - 1);
        repository.save(updatedReadModel);
        emitUpdate(processingContext, updatedReadModel);
    }


    /**
     * Emits an update for subscription queries when course stats change.
     * This should be called by the projection when the read model is updated.
     */
    public void emitUpdate(ProcessingContext processingContext, CoursesStatsReadModel updatedStats) {
        QueryUpdateEmitter updateEmitter = QueryUpdateEmitter.forContext(processingContext);

        // Emit update for single course stats subscription
        updateEmitter.emit(
                GetCourseStatsById.class,
                query -> true,
                new GetCourseStatsById.Result(updatedStats)
        );

        // Emit update for all course stats subscription
        updateEmitter.emit(
                GetAllCourseStats.class,
                query -> true,
                new GetAllCourseStats.Result(repository.findAll())
        );
    }

}
