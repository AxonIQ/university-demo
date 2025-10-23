package io.axoniq.demo.university;

import io.axoniq.demo.university.faculty.write.classic.EnrollStudent;
import io.axoniq.demo.university.faculty.write.createcourse.CreateCourse;
import io.axoniq.demo.university.faculty.write.renamecourse.RenameCourse;
import io.axoniq.demo.university.shared.application.notifier.NotificationService;
import io.axoniq.demo.university.shared.ids.CourseId;
import io.axoniq.demo.university.shared.ids.StudentId;
import io.axoniq.demo.university.shared.infrastructure.notifier.LoggingNotificationService;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.configuration.ComponentRegistry;
import org.axonframework.eventhandling.processors.streaming.token.store.TokenStore;
import org.axonframework.eventhandling.processors.streaming.token.store.inmemory.InMemoryTokenStore;
import org.axonframework.extension.spring.config.SpringComponentRegistry;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootApplication
public class UniversitySpringBootAxonApplication {
  public static void main(String[] args) {
    SpringApplication.run(UniversitySpringBootAxonApplication.class, args);
  }

  @Bean
  public NotificationService notificationService() {
    return new LoggingNotificationService();
  }

  @Bean
  public TokenStore tokenStore() {
    return new InMemoryTokenStore();
  }

  @Component
  static class MySampleApplicationRunner implements ApplicationRunner {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final ComponentRegistry componentRegistry;

    MySampleApplicationRunner(ComponentRegistry componentRegistry) {
      this.componentRegistry = componentRegistry;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
      try {
        var courseId = CourseId.random();
        var createCourse = new CreateCourse(courseId, "Event Sourcing in Practice", 3);
        var renameCourse = new RenameCourse(courseId, "Advanced Event Sourcing");
        var studentId = StudentId.random();
        var enrollStudent = new EnrollStudent(studentId, "Kermit", "The Frog");
        var commandGateway = ((SpringComponentRegistry) this.componentRegistry).configuration().getComponent(CommandGateway.class);

        commandGateway.sendAndWait(createCourse);
        commandGateway.sendAndWait(renameCourse);
        commandGateway.sendAndWait(enrollStudent);
        logger.info("Successfully executed sample commands");
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Error while executing sample commands: " + e.getMessage(), e);
      }
    }
  }
}
