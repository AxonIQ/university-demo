package io.axoniq.demo.university;

import io.axoniq.demo.university.faculty.FacultyModuleConfiguration;
import io.axoniq.demo.university.faculty.events.StudentSubscribedToCourse;
import jakarta.annotation.Nonnull;
import org.assertj.core.api.Assertions;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.configuration.AxonConfiguration;
import org.axonframework.configuration.Configuration;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.TrackingToken;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventstreaming.StreamableEventSource;
import org.axonframework.eventstreaming.StreamingCondition;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.MessageStream;
import org.axonframework.test.fixture.MessagesRecordingConfigurationEnhancer;
import org.axonframework.test.fixture.RecordingEventStore;
import org.axonframework.test.server.AxonServerContainerUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class UniversityApplicationTest {

    protected AxonConfiguration sut;

    @BeforeEach
    void beforeEach() throws IOException {
        var properties = overrideProperties(ConfigurationProperties.load());
        if (properties.axonServerEnabled()) {
            AxonServerContainerUtils.purgeEventsFromAxonServer("localhost",
                    8024,
                    "university",
                    AxonServerContainerUtils.DCB_CONTEXT);
        }
        var configurer = new UniversityAxonApplication().configurer(properties, this::configureTestApplication);
        sut = configurer.start();
    }

    private EventSourcingConfigurer configureTestApplication(EventSourcingConfigurer configurer) {
        configurer = configurer.componentRegistry(cr -> cr.registerEnhancer(new MessagesRecordingConfigurationEnhancer()));
        configurer = useEventStorageEngineAsEventSource(configurer);
        configurer = overrideConfigurer(configurer);
        return configurer;
    }

    private static EventSourcingConfigurer useEventStorageEngineAsEventSource(EventSourcingConfigurer eventSourcingConfigurer) {
        return eventSourcingConfigurer.messaging(m -> m.eventProcessing(
                        ep -> ep.pooledStreaming(
                                ps -> ps.defaults((cfg, d) -> d.eventSource(eventSourceFromEventStorageEngine(cfg)))
                        )
                )
        );
    }

    private static StreamableEventSource<EventMessage> eventSourceFromEventStorageEngine(Configuration configuration) {
        var eventStorageEngine = configuration.getComponent(EventStorageEngine.class);
        return new StreamableEventSource<>() {
            @Override
            public MessageStream<EventMessage> open(@Nonnull StreamingCondition condition) {
                return eventStorageEngine.stream(condition);
            }

            @Override
            public CompletableFuture<TrackingToken> firstToken() {
                return eventStorageEngine.firstToken();
            }

            @Override
            public CompletableFuture<TrackingToken> latestToken() {
                return eventStorageEngine.latestToken();
            }

            @Override
            public CompletableFuture<TrackingToken> tokenAt(@Nonnull Instant at) {
                return eventStorageEngine.tokenAt(at);
            }
        };
    }

    @AfterEach
    void afterEach() {
        sut.shutdown();
    }

    protected void eventOccurred(Object event) {
        eventsOccurred(event);
    }

    protected void eventsOccurred(Object... events) {
        var eventGateway = sut.getComponent(EventGateway.class);
        for (Object event : events) {
            eventGateway.publish(null, event);
        }
        var eventStore = (RecordingEventStore) sut.getComponent(EventStore.class);
        eventStore.reset();
    }

    protected void eventsOccurred(List<Object> events) {
        var eventGateway = sut.getComponent(EventGateway.class);
        events.forEach(e -> eventGateway.publish(null, e));
        var eventStore = (RecordingEventStore) sut.getComponent(EventStore.class);
        eventStore.reset();
    }

    protected void executeCommand(Object command) {
        var commandGateway = sut.getComponent(CommandGateway.class);
        commandGateway.sendAndWait(command);
    }

    protected void assertEvents(Object... events) {
        var eventStore = (RecordingEventStore) sut.getComponent(EventStore.class);
        Assertions.assertThat(eventStore.recorded().stream().map(Message::payload)).contains(events);
    }

    protected void assertNoEvents(){
        var eventStore = (RecordingEventStore) sut.getComponent(EventStore.class);
        Assertions.assertThat(eventStore.recorded().stream().map(Message::payload)).isEmpty();
    }

    protected ConfigurationProperties overrideProperties(ConfigurationProperties properties) {
        return properties;
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
