package io.axoniq.demo.university.faculty;

import io.axoniq.demo.university.UniversityAxonApplication;
import org.axonframework.common.ReflectionUtils;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventstreaming.StreamableEventSource;
import org.axonframework.test.fixture.AxonTestFixture;
import org.axonframework.test.fixture.RecordingEventStore;

public class FacultyTestFixture {

    public static AxonTestFixture create() {
        var application = new UniversityAxonApplication();
        var configurer = application.configurer();
        useRecordingEventStoreAsStreamableEventSource(configurer);
        return AxonTestFixture.with(configurer);
    }

    public static void useRecordingEventStoreAsStreamableEventSource(EventSourcingConfigurer configurer) {
        configurer.componentRegistry(cr -> cr.registerComponent(StreamableEventSource.class, c -> {
            var eventStore = (RecordingEventStore) c.getComponent(EventStore.class);
            try {
                return ReflectionUtils.getFieldValue(
                        RecordingEventStore.class.getSuperclass().getDeclaredField("delegate"),
                        eventStore
                );
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    private FacultyTestFixture() {

    }
}
