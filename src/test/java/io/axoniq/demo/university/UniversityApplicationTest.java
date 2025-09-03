package io.axoniq.demo.university;

import io.axoniq.demo.university.faculty.FacultyModuleConfiguration;
import org.axonframework.configuration.AxonConfiguration;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.axonframework.test.server.AxonServerContainerUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;

public abstract class UniversityApplicationTest {

    protected AxonConfiguration sut;

    @BeforeEach
    void beforeEach() throws IOException {
        var properties = properties(ConfigurationProperties.load());
        if (properties.axonServerEnabled()) {
            AxonServerContainerUtils.purgeEventsFromAxonServer("localhost",
                    8024,
                    "university",
                    AxonServerContainerUtils.DCB_CONTEXT);
        }
        var configurer = new UniversityAxonApplication()
                .configurer(properties, c -> configurer(FacultyModuleConfiguration.configure(c)));
        sut = configurer.start();
    }

    @AfterEach
    void afterEach() {
        sut.shutdown();
    }

    protected ConfigurationProperties properties(ConfigurationProperties properties) {
        return properties;
    }

    protected EventSourcingConfigurer configurer(EventSourcingConfigurer configurer) {
        return configurer;
    }
}
