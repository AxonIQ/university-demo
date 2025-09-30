package io.axoniq.demo.university;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.axoniq.demo.university.faculty.FacultyModuleConfiguration;
import io.axoniq.demo.university.shared.ids.CourseId;
import io.axoniq.demo.university.faculty.write.createcourseplain.CreateCourse;
import io.axoniq.demo.university.faculty.write.renamecourse.RenameCourse;
import io.axoniq.framework.postgresql.DataSourceConnectionExecutor;
import io.axoniq.framework.postgresql.PostgresqlEventStorageEngine;
import org.axonframework.axonserver.connector.AxonServerConfiguration;
import org.axonframework.axonserver.connector.AxonServerConfigurationEnhancer;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.common.infra.FilesystemStyleComponentDescriptor;
import org.axonframework.configuration.AxonConfiguration;
import org.axonframework.eventhandling.conversion.DelegatingEventConverter;
import org.axonframework.eventhandling.conversion.EventConverter;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;

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
        if (configProps.isAxonServerEventStorageEngine()) {
            configurer.componentRegistry(r -> r.registerComponent(AxonServerConfiguration.class, c -> {
                var axonServerConfig = new AxonServerConfiguration();
                axonServerConfig.setContext(CONTEXT);
                return axonServerConfig;
            }));
        } else {
            configurer.componentRegistry(r -> r.disableEnhancer(AxonServerConfigurationEnhancer.class));
        }
        if (configProps.isPostgresEventStorageEngine()) {
            HikariConfig config = new HikariConfig();

            config.setJdbcUrl("jdbc:postgresql://localhost:5444/university_demo_db");
            config.setUsername("university_demo_user");
            config.setPassword("university_demo_password");
//            config.setMaximumPoolSize(5);
//            config.setMinimumIdle(1);
//            config.setAutoCommit(false);

            configurer.registerEventStorageEngine(cr ->
                    new PostgresqlEventStorageEngine(
                            new DataSourceConnectionExecutor(new HikariDataSource(config)),
                            cr.getComponent(EventConverter.class)
                    )
            );
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
