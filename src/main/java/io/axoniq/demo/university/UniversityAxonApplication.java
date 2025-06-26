package io.axoniq.demo.university;

import io.axoniq.demo.university.faculty.FacultyModuleConfiguration;
import io.axoniq.demo.university.shared.ids.CourseId;
import io.axoniq.demo.university.faculty.write.createcourseplain.CreateCourse;
import io.axoniq.demo.university.faculty.write.renamecourse.RenameCourse;
import org.axonframework.axonserver.connector.AxonServerConnectionManager;
import org.axonframework.axonserver.connector.TestConverter;
import org.axonframework.axonserver.connector.event.AxonServerEventStorageEngine;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.common.infra.FilesystemStyleComponentDescriptor;
import org.axonframework.configuration.ApplicationConfigurer;
import org.axonframework.configuration.AxonConfiguration;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;

import java.util.logging.Level;
import java.util.logging.Logger;

public class UniversityAxonApplication {

    private static final Logger logger = Logger.getLogger(UniversityAxonApplication.class.getName());

    public static void main(String[] args) {
        ConfigurationProperties configProps = ConfigurationProperties.fromArgs(args);
        var configuration = startApplication(configProps);
        printApplicationConfiguration(configuration);
        executeSampleCommands(configuration);
    }

    private static AxonConfiguration startApplication(ConfigurationProperties configProps) {
        var configurer = new UniversityAxonApplication().configurer(configProps);
        return configurer.start();
    }

    private static void printApplicationConfiguration(AxonConfiguration configuration) {
        var componentDescriptor = new FilesystemStyleComponentDescriptor();
        componentDescriptor.describeProperty("configuration", configuration);
        logger.info("Application started with following configuration: \n" + componentDescriptor.describe());
    }

    public ApplicationConfigurer configurer() {
        return configurer(ConfigurationProperties.defaults());
    }

    public ApplicationConfigurer configurer(ConfigurationProperties configProps) {
        var configurer = EventSourcingConfigurer.create();
        configurer = FacultyModuleConfiguration.configure(configurer);
        if (configProps.axonServerEnabled) {
            configurer = configurer
                    .registerEventStorageEngine(c -> new AxonServerEventStorageEngine(
                            c.getComponent(AxonServerConnectionManager.class).getConnection("university"),
                            new TestConverter() // TODO: Replace with actual converter in 5.0.0-M3
                    ));
        }
        return configurer;
    }

    private static void executeSampleCommands(AxonConfiguration configuration) {
        try {
            var courseId = CourseId.random();
            var createCourse = new CreateCourse(courseId, "Event Sourcing in Practice", 3);
            var renameCourse = new RenameCourse(courseId, "Advanced Event Sourcing");

            var commandGateway = configuration.getComponent(CommandGateway.class);
            commandGateway.sendAndWait(createCourse);
            commandGateway.sendAndWait(renameCourse);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while executing sample commands: " + e.getMessage(), e);
        }
    }

    static class ConfigurationProperties {
        boolean axonServerEnabled = false;

        public static ConfigurationProperties defaults() {
            return new ConfigurationProperties();
        }

        public static ConfigurationProperties fromArgs(String[] args) {
            ConfigurationProperties props = new ConfigurationProperties();
            for (String arg : args) {
                if (arg.startsWith("axon.server.enabled=")) {
                    String value = arg.substring("axon.server.enabled=".length());
                    props.axonServerEnabled = Boolean.parseBoolean(value);
                }
            }
            return props;
        }
    }
}
