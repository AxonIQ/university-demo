package io.axoniq.demo.university.faculty;

import io.axoniq.demo.university.ConfigurationProperties;
import io.axoniq.demo.university.UniversityAxonApplication;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.axonframework.test.fixture.AxonTestFixture;
import org.axonframework.test.server.AxonServerContainerUtils;

import java.io.IOException;
import java.util.function.UnaryOperator;

public class FacultyAxonTestFixture {

    public static AxonTestFixture app() {
        return slice(FacultyModuleConfiguration::configure);
    }

    public static AxonTestFixture slice(UnaryOperator<EventSourcingConfigurer> customization) {
        var application = new UniversityAxonApplication();
        var configuration = ConfigurationProperties.load();
        var configurer = application.configurer(configuration, customization);
        purgeAxonServerIfEnabled(configuration);
        return AxonTestFixture.with(configurer, c -> configuration.isAxonServerEventStorageEngine() ? c : c.disableAxonServer());
    }

    private static void purgeAxonServerIfEnabled(ConfigurationProperties configuration) {
        boolean useAxonServer = configuration.isAxonServerEventStorageEngine();
        if (useAxonServer) {
            try {
                AxonServerContainerUtils.purgeEventsFromAxonServer("localhost", 8024, "university", true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private FacultyAxonTestFixture() {

    }
}
