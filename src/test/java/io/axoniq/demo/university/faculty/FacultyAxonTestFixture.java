package io.axoniq.demo.university.faculty;

import io.axoniq.demo.university.ConfigurationProperties;
import io.axoniq.demo.university.UniversityAxonApplication;
import org.axonframework.common.ReflectionUtils;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventstreaming.StreamableEventSource;
import org.axonframework.test.fixture.AxonTestFixture;
import org.axonframework.test.fixture.RecordingEventStore;

import java.util.function.UnaryOperator;

public class FacultyAxonTestFixture {

    public static AxonTestFixture app() {
        return slice(FacultyModuleConfiguration::configure);
    }

    public static AxonTestFixture slice(UnaryOperator<EventSourcingConfigurer> customization) {
        var application = new UniversityAxonApplication();
        ConfigurationProperties configuration = ConfigurationProperties.load();
        var configurer = application.configurer(configuration, customization);
        return AxonTestFixture.with(configurer, c -> c.axonServerEnabled(configuration.axonServerEnabled()));
    }

    private FacultyAxonTestFixture() {

    }
}
