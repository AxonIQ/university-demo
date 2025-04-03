package io.axoniq.demo.university.faculty.write.renamecourse;

import io.axoniq.demo.university.faculty.write.CourseId;
import org.axonframework.commandhandling.annotation.AnnotatedCommandHandlingComponent;
import org.axonframework.config.ConfigurationParameterResolverFactory;
import org.axonframework.configuration.NewConfiguration;
import org.axonframework.eventsourcing.configuration.EventSourcedEntityBuilder;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.axonframework.messaging.annotation.ClasspathParameterResolverFactory;
import org.axonframework.messaging.annotation.MultiParameterResolverFactory;
import org.axonframework.modelling.StateManager;
import org.axonframework.modelling.command.annotation.InjectEntityParameterResolverFactory;
import org.axonframework.modelling.configuration.StatefulCommandHandlingModule;

import java.util.List;

public class RenameCourseConfiguration {

    public static EventSourcingConfigurer configure(EventSourcingConfigurer configurer) {
        var stateEntity = EventSourcedEntityBuilder
                .annotatedEntity(CourseId.class, RenameCourseCommandHandler.State.class);
        var commandHandlingModule = StatefulCommandHandlingModule
                .named("RenameCourse")
                .entities()
                .entity(stateEntity)
                .commandHandlers()
                .commandHandlingComponent(c -> new AnnotatedCommandHandlingComponent<>(new RenameCourseCommandHandler(),
                                                                                       parameterResolverFactory(c)));
        return configurer.registerStatefulCommandHandlingModule(commandHandlingModule);
    }

    private static MultiParameterResolverFactory parameterResolverFactory(NewConfiguration configuration) {
        return MultiParameterResolverFactory.ordered(List.of(
                ClasspathParameterResolverFactory.forClass(RenameCourseConfiguration.class),
                // To be able to get components
                new ConfigurationParameterResolverFactory(configuration),
                // To be able to get the entity, the StateManager needs to be available.
                // When the new configuration API is there, we should have a way to resolve this
                new InjectEntityParameterResolverFactory(configuration.getComponent(StateManager.class))
        ));
    }
}
