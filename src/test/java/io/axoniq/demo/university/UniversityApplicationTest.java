package io.axoniq.demo.university;

import io.axoniq.demo.university.faculty.FacultyModuleConfiguration;
import jakarta.annotation.Nonnull;
import org.assertj.core.api.Assertions;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.configuration.AxonConfiguration;
import org.axonframework.configuration.Configuration;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.eventhandling.processors.streaming.token.TrackingToken;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventstreaming.StreamableEventSource;
import org.axonframework.eventstreaming.StreamingCondition;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.MessageStream;
import org.axonframework.messaging.unitofwork.UnitOfWorkFactory;
import org.axonframework.test.fixture.MessagesRecordingConfigurationEnhancer;
import org.axonframework.test.fixture.RecordingEventStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class UniversityApplicationTest {

    protected AxonConfiguration configuration;

    @BeforeEach
    void beforeEach() {
        var properties = overrideProperties(ConfigurationProperties.load());
        var configurer = new UniversityAxonApplication().configurer(properties, this::configureTestApplication);
        configuration = configurer.start();
    }

    @AfterEach
    void afterEach() {
        configuration.shutdown();
    }

    private EventSourcingConfigurer configureTestApplication(EventSourcingConfigurer configurer) {
        configurer = configurer.componentRegistry(cr -> cr.registerEnhancer(new MessagesRecordingConfigurationEnhancer()));
        configurer = overrideConfigurer(configurer);
        return configurer;
    }

    protected void eventOccurred(Object event) {
        eventsOccurred(event);
    }

    protected void eventsOccurred(Object... events) {
        eventsOccurred(List.of(events));
    }

    protected void eventsOccurred(List<Object> events) {
        var eventGateway = configuration.getComponent(EventGateway.class);
        var unitOfWork = configuration.getComponent(UnitOfWorkFactory.class).create();
        unitOfWork.onInvocation(ctx -> eventGateway.publish(ctx, events));
        unitOfWork.execute().join();
        var eventStore = (RecordingEventStore) configuration.getComponent(EventStore.class);
        eventStore.reset();
    }

    protected void executeCommand(Object command) {
        var commandGateway = configuration.getComponent(CommandGateway.class);
        commandGateway.sendAndWait(command);
    }

    protected void assertEvents(Object... events) {
        var eventStore = (RecordingEventStore) configuration.getComponent(EventStore.class);
        Assertions.assertThat(eventStore.recorded().stream().map(Message::payload)).contains(events);
    }

    protected void assertNoEvents() {
        var eventStore = (RecordingEventStore) configuration.getComponent(EventStore.class);
        Assertions.assertThat(eventStore.recorded().stream().map(Message::payload)).isEmpty();
    }

    protected ConfigurationProperties overrideProperties(ConfigurationProperties properties) {
        return properties.axonServerEnabled(true);
    }

    /**
     * By default, the whole Faculty Module is configured, but you can easily test only parts of it (certain app modules).
     *
     * @param configurer
     * @return
     */
    protected EventSourcingConfigurer overrideConfigurer(EventSourcingConfigurer configurer) {
        return FacultyModuleConfiguration.configure(configurer);
    }
}
