package io.axoniq.demo.university.faculty.write.renamecourse;

import io.axoniq.demo.university.faculty.events.CourseCreated;
import io.axoniq.demo.university.faculty.events.CourseRenamed;
import io.axoniq.demo.university.faculty.write.CourseId;
import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.EventSink;
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.eventsourcing.annotation.EventSourcedEntity;
import org.axonframework.messaging.MessageType;
import org.axonframework.messaging.unitofwork.ProcessingContext;
import org.axonframework.modelling.command.annotation.InjectEntity;

import java.util.List;
import java.util.stream.Collectors;

class RenameCourseCommandHandler {

    @CommandHandler
    public void handle(
            RenameCourse command,
            @InjectEntity State state,
            EventSink eventSink,
            ProcessingContext processingContext
    ) {
        var events = decide(command, state);
        eventSink.publish(processingContext, toMessages(events));
    }

    private List<CourseRenamed> decide(RenameCourse command, State state) {
        if (state.name.equals(command.name())) {
            return List.of();
        }
        return List.of(new CourseRenamed(command.courseId().raw(), command.name()));
    }

    private static List<EventMessage<?>> toMessages(List<CourseRenamed> events) {
        return events.stream()
                     .map(RenameCourseCommandHandler::toMessage)
                     .collect(Collectors.toList());
    }

    private static EventMessage<?> toMessage(Object payload) {
        return new GenericEventMessage<>(
                new MessageType(payload.getClass()),
                payload
        );
    }

    @EventSourcedEntity
    public static class State {

        private final CourseId courseId;
        private String name;

        public State(CourseId courseId) {
            this.courseId = courseId;
        }

        @EventSourcingHandler
        public void evolve(CourseCreated event) {
            this.name = event.name();
        }
    }
}


/**
 * class RenameCourseCommandHandler implements StatefulCommandHandler {
 *
 *     @Override
 *     @Nonnull
 *     public MessageStream.Single<? extends CommandResultMessage<?>> handle(
 *             @Nonnull CommandMessage<?> command,
 *             @Nonnull StateManager state,
 *             @Nonnull ProcessingContext context
 *     ) {
 *         return null;
 *     }
 */