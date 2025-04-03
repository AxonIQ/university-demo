package io.axoniq.demo.university.faculty.write.createcourse;

import io.axoniq.demo.university.faculty.events.CourseCreated;
import io.axoniq.demo.university.faculty.write.CourseId;
import jakarta.annotation.Nonnull;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.EventSink;
import org.axonframework.eventsourcing.EventStateApplier;
import org.axonframework.eventsourcing.configuration.EventSourcedEntityBuilder;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.axonframework.eventsourcing.eventstore.EventCriteria;
import org.axonframework.eventsourcing.eventstore.Tag;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.QualifiedName;
import org.axonframework.messaging.unitofwork.ProcessingContext;
import org.axonframework.modelling.command.EntityIdResolver;
import org.axonframework.modelling.configuration.StatefulCommandHandlingModule;

public class CreateCourseConfiguration {

    public static EventSourcingConfigurer configure(EventSourcingConfigurer configurer) {
        var stateEntity = EventSourcedEntityBuilder
                .entity(CourseId.class, CreateCourseCommandHandler.State.class)
                .entityFactory(c -> (type, id) -> CreateCourseCommandHandler.State.initial())
                .criteriaResolver(c -> id -> EventCriteria.match()
                                                          .eventsOfTypes(CourseCreated.TYPE)
                                                          .withTags(new Tag("Course", id.raw())))
                .eventStateApplier(c -> new CourseEventStateApplier());
        var commandHandlingModule = StatefulCommandHandlingModule
                .named("CreateCourse")
                .entities()
                .entity(stateEntity)
                .commandHandlers()
                .commandHandler(new QualifiedName(CreateCourse.class),
                                c -> new CreateCourseCommandHandler(c.getComponent(EventSink.class)));
        return configurer.registerStatefulCommandHandlingModule(commandHandlingModule);
    }

    public static class CourseEventStateApplier implements EventStateApplier<CreateCourseCommandHandler.State> {


        @Override
        public CreateCourseCommandHandler.State apply(@Nonnull CreateCourseCommandHandler.State model,
                                                      @Nonnull EventMessage<?> event,
                                                      @Nonnull ProcessingContext processingContext
        ) {
            var payload = event.getPayload();
            return payload instanceof CourseCreated courseCreated
                    ? model.apply(courseCreated)
                    : model;
        }
    }

    public static class CourseIdResolver implements EntityIdResolver<CourseId> {

        @Nonnull
        @Override
        public CourseId resolve(@Nonnull Message<?> message, @Nonnull ProcessingContext context) {
            var id = resolveOrNull(message);
            if (id == null) {
                throw new IllegalArgumentException("Cannot resolve course courseId from the command");
            }
            return id;
        }

        private static CourseId resolveOrNull(Message<?> message) {
            var payload = message.getPayload();
            return payload instanceof CreateCourse createCourse
                    ? createCourse.courseId()
                    : null;
        }
    }
}
