package io.axoniq.demo.university;

import io.axoniq.demo.university.faculty.FacultyModuleConfiguration;
import io.axoniq.demo.university.faculty.write.createcourseplain.CreateCourse;
import io.axoniq.demo.university.faculty.write.renamecourse.RenameCourse;
import io.axoniq.demo.university.shared.ids.CourseId;
import org.axonframework.axonserver.connector.AxonServerConfiguration;
import org.axonframework.axonserver.connector.AxonServerConfigurationEnhancer;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.common.infra.FilesystemStyleComponentDescriptor;
import org.axonframework.configuration.AxonConfiguration;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.axonframework.monitoring.MessageMonitor;
import org.axonframework.queryhandling.SubscriptionQueryUpdateMessage;
import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UniversityAxonApplication {

    private static final String CONTEXT = "university";
    private static final Logger logger = Logger.getLogger(UniversityAxonApplication.class.getName());

    public static void main(String[] args) {
        ConfigurationProperties configProps = ConfigurationProperties.load();
        var configuration = startApplication(configProps);
        executeSampleCommands(configuration);
        configuration.shutdown();
    }

    public static AxonConfiguration startApplication() {
        return startApplication(ConfigurationProperties.load());
    }

    public static AxonConfiguration startApplication(ConfigurationProperties configProps) {
        var configurer = new UniversityAxonApplication().configurer(configProps, FacultyModuleConfiguration::configure);


        configurer.messaging(m -> {
            m.registerSubscriptionQueryUpdateMonitor(c -> new MessageMonitor<SubscriptionQueryUpdateMessage>() {
                @Override
                public MonitorCallback onMessageIngested(@NotNull SubscriptionQueryUpdateMessage message) {
                    logger.info("Received subscription query update message: " + message);
                    return new MonitorCallback() {
                        @Override
                        public void reportSuccess() {
                            logger.info("Successfully processed subscription query update message: " + message);
                        }

                        @Override
                        public void reportFailure(Throwable cause) {
                            logger.log(Level.SEVERE, "Failed to process subscription query update message: " + message, cause);
                        }

                        @Override
                        public void reportIgnored() {
                            logger.info("Ignored subscription query update message: " + message);
                        }
                    };
                }
            });

        });
        var configuration = configurer.start();
        printApplicationConfiguration(configuration);
        return configuration;
    }

    private static void printApplicationConfiguration(AxonConfiguration configuration) {
        var componentDescriptor = new FilesystemStyleComponentDescriptor();
        componentDescriptor.describeProperty("configuration", configuration);
        logger.info("Application started with following configuration: \n" + componentDescriptor.describe());
    }

    public EventSourcingConfigurer configurer() {
        return configurer(ConfigurationProperties.load(), FacultyModuleConfiguration::configure);
    }

    public EventSourcingConfigurer configurer(
            ConfigurationProperties configProps,
            UnaryOperator<EventSourcingConfigurer> customization
    ) {
        var configurer = EventSourcingConfigurer.create();
        if (configProps.axonServerEnabled) {
            configurer.componentRegistry(r -> r.registerComponent(AxonServerConfiguration.class, c -> {
                var axonServerConfig = new AxonServerConfiguration();
                axonServerConfig.setContext(CONTEXT);
                return axonServerConfig;
            }));
        } else {
            configurer.componentRegistry(r -> r.disableEnhancer(AxonServerConfigurationEnhancer.class));
        }
        configurer = customization.apply(configurer);
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
            logger.info("Successfully executed sample commands");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while executing sample commands: " + e.getMessage(), e);
        }
    }

}
