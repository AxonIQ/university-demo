package io.axoniq.demo.university.faculty.write.createcourseplain;

import io.axoniq.demo.university.faculty.events.CourseCreated;
import io.axoniq.demo.university.shared.slices.write.CommandResult;
import jakarta.annotation.Nonnull;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.GenericCommandResultMessage;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.EventSink;
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.eventhandling.gateway.EventAppender;
import org.axonframework.messaging.MessageStream;
import org.axonframework.messaging.MessageType;
import org.axonframework.messaging.MessageTypeResolver;
import org.axonframework.messaging.unitofwork.ProcessingContext;
import org.axonframework.modelling.StateManager;
import org.axonframework.modelling.command.StatefulCommandHandler;

import java.util.List;
import java.util.stream.Collectors;

class CreateCourseCommandHandler implements StatefulCommandHandler {

    private final EventSink eventSink;
    private final MessageTypeResolver messageTypeResolver;

    CreateCourseCommandHandler(EventSink eventSink, MessageTypeResolver messageTypeResolver) {
        this.eventSink = eventSink;
        this.messageTypeResolver = messageTypeResolver;
    }

    @Override
    @Nonnull
    public MessageStream.Single<? extends CommandResultMessage<?>> handle(
            @Nonnull CommandMessage<?> command,
            @Nonnull StateManager state,
            @Nonnull ProcessingContext context
    ) {
        var eventAppender = EventAppender.forContext(context, eventSink, messageTypeResolver);
        var payload = (CreateCourse) command.getPayload();
        var decideFuture = state
                .loadEntity(State.class, payload.courseId(), context)
                .thenApply(entity -> decide(payload, entity))
                .thenAccept(eventAppender::append)
                .thenApply(r -> new GenericCommandResultMessage<>(messageTypeResolver.resolve(CommandResult.class),
                                                                  new CommandResult(payload.courseId().raw())));
        return MessageStream.fromFuture(decideFuture);
    }

    private List<CourseCreated> decide(CreateCourse command, State state) {
        if (state.created) {
            return List.of();
        }
        return List.of(new CourseCreated(command.courseId().raw(), command.name(), command.capacity()));
    }

    static final class State {

        private boolean created;

        private State(boolean created) {
            this.created = created;
        }

        static State initial() {
            return new State(false);
        }

        State evolve(CourseCreated event) {
            this.created = true;
            return this;
        }
    }
}
