package io.axoniq.demo.university;

import io.axoniq.demo.university.faculty.read.coursestats.CoursesQueryResult;
import io.axoniq.demo.university.faculty.read.coursestats.GetCourseStatsById;
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
import org.axonframework.queryhandling.gateway.QueryGateway;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

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
  @Profile("autorun")
  static class MySampleApplicationRunner implements ApplicationRunner {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ComponentRegistry componentRegistry;

    MySampleApplicationRunner(ComponentRegistry componentRegistry) {
      this.componentRegistry = componentRegistry;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
      try {
        var courseId = CourseId.random();
        logger.info("=".repeat(80));
        logger.info("Open browser at: http://localhost:8080/course-stats-demo.html");
        logger.info("=".repeat(80));

        var commandGateway = ((SpringComponentRegistry) this.componentRegistry).configuration().getComponent(CommandGateway.class);

        var queryGateway = ((SpringComponentRegistry) this.componentRegistry).configuration().getComponent(QueryGateway.class);

        var countDownLatch = new CountDownLatch(3);

        var queryResult = queryGateway
          .subscriptionQuery(
            new GetCourseStatsById(courseId),
            CoursesQueryResult.class
          );

        // Subscribe to the initial result
        queryResult.subscribe(
          new Subscriber<>() {
            @Override
            public void onSubscribe(Subscription s) {

            }

            @Override
            public void onNext(CoursesQueryResult result) {
              logger.info("Initial result received: " + result);
              countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {

            }
          }
        );

        var createCourse = new CreateCourse(courseId, "Event Sourcing in Practice", 3);
        var renameCourse = new RenameCourse(courseId, "Advanced Event Sourcing");
        var studentId = StudentId.random();
        var enrollStudent = new EnrollStudent(studentId, "Kermit", "The Frog");

        commandGateway.sendAndWait(createCourse);
        commandGateway.sendAndWait(renameCourse);
        commandGateway.sendAndWait(enrollStudent);

        logger.info("Successfully executed sample commands");

        Thread.sleep(1000);

        var found = queryGateway
          .query(new GetCourseStatsById(courseId), CoursesQueryResult.class, null)
          .join();
        logger.info("Found: {}", found);

        // Subscription Query Example - subscribes to initial result and future updates
        logger.info("Starting subscription query for course stats...");

        // Keep subscription alive for a few seconds to demonstrate updates
        countDownLatch.await();

        logger.info("Subscription closed");

      } catch (Exception e) {
        logger.error("Error while executing sample commands: " + e.getMessage(), e);
      }
    }
  }
}
