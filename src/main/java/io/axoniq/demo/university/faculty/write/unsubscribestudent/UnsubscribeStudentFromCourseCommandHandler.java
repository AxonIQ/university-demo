package io.axoniq.demo.university.faculty.write.unsubscribestudent;

import io.axoniq.demo.university.faculty.FacultyTags;
import io.axoniq.demo.university.faculty.events.StudentSubscribedToCourse;
import io.axoniq.demo.university.faculty.events.StudentUnsubscribedFromCourse;
import io.axoniq.demo.university.faculty.write.CourseId;
import io.axoniq.demo.university.faculty.write.StudentId;
import jakarta.annotation.Nonnull;
import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.EventSink;
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.eventsourcing.annotation.EventCriteriaBuilder;
import org.axonframework.eventsourcing.annotation.EventSourcedEntity;
import org.axonframework.eventsourcing.eventstore.EventCriteria;
import org.axonframework.eventsourcing.eventstore.Tag;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.MessageType;
import org.axonframework.messaging.unitofwork.ProcessingContext;
import org.axonframework.modelling.annotation.InjectEntity;
import org.axonframework.modelling.command.EntityIdResolver;

import java.util.List;
import java.util.stream.Collectors;

class UnsubscribeStudentFromCourseCommandHandler {

    @CommandHandler
    void handle(
            UnsubscribeStudentFromCourse command,
            @InjectEntity(idResolver = SubscriptionIdResolver.class) State state,
            EventSink eventSink,
            ProcessingContext processingContext
    ) {
        var events = decide(command, state);
        eventSink.publish(processingContext, toMessages(events));
    }

    private List<StudentUnsubscribedFromCourse> decide(UnsubscribeStudentFromCourse command, State state) {
        return state.subscribed
                ? List.of(new StudentUnsubscribedFromCourse(command.studentId().raw(), command.courseId().raw()))
                : List.of();
    }

    private static List<EventMessage<?>> toMessages(List<StudentUnsubscribedFromCourse> events) {
        return events.stream()
                     .map(UnsubscribeStudentFromCourseCommandHandler::toMessage)
                     .collect(Collectors.toList());
    }

    private static EventMessage<?> toMessage(Object payload) {
        return new GenericEventMessage<>(
                new MessageType(payload.getClass()),
                payload
        );
    }

    @EventSourcedEntity
    static final class State {

        boolean subscribed = false;

        @EventSourcingHandler
        void apply(StudentSubscribedToCourse event) {
            this.subscribed = true;
        }

        @EventSourcingHandler
        void apply(StudentUnsubscribedFromCourse event) {
            this.subscribed = false;
        }

        @EventCriteriaBuilder
        private static EventCriteria resolveCriteria(SubscriptionId id) {
            var courseId = id.courseId().raw();
            var studentId = id.studentId().raw();
            return EventCriteria.match()
                                .eventsOfTypes(
                                        StudentSubscribedToCourse.class.getName(),
                                        StudentUnsubscribedFromCourse.class.getName()
                                ).withTags(Tag.of(FacultyTags.COURSE_ID, courseId),
                                           Tag.of(FacultyTags.STUDENT_ID, studentId));
        }
    }

    private static class SubscriptionIdResolver implements EntityIdResolver<SubscriptionId> {

        @Override
        @Nonnull
        public SubscriptionId resolve(@Nonnull Message<?> command, @Nonnull ProcessingContext context) {
            if (command.getPayload() instanceof UnsubscribeStudentFromCourse(StudentId studentId, CourseId courseId)) {
                return new SubscriptionId(courseId, studentId);
            }
            throw new IllegalArgumentException("Can not resolve SubscriptionId from command");
        }
    }
}
