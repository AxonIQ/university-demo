package io.axoniq.demo.university.faculty.automation.allcoursesfullybookednotifier;

import io.axoniq.demo.university.ConfigurationProperties;
import io.axoniq.demo.university.UniversityAxonApplication;
import io.axoniq.demo.university.shared.application.notifier.NotificationService;
import io.axoniq.demo.university.shared.infrastructure.notifier.RecordingNotificationService;
import io.axoniq.demo.university.faculty.events.CourseCreated;
import io.axoniq.demo.university.faculty.events.StudentSubscribedToCourse;
import io.axoniq.demo.university.shared.ids.CourseId;
import io.axoniq.demo.university.shared.ids.StudentId;
import org.axonframework.configuration.AxonConfiguration;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.messaging.unitofwork.UnitOfWorkFactory;
import org.axonframework.test.fixture.RecordingEventStore;
import org.axonframework.test.server.AxonServerContainerUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class WhenAllCoursesFullyBookedThenSendNotificationTest {

    private AxonConfiguration sut;

    @BeforeEach
    void beforeEach() throws IOException {
        var properties = ConfigurationProperties.load();
        if (properties.axonServerEnabled()) {
            AxonServerContainerUtils.purgeEventsFromAxonServer("localhost",
                    8024,
                    "university",
                    AxonServerContainerUtils.DCB_CONTEXT);
        }
        sut = UniversityAxonApplication.startApplication(properties);
    }

    @AfterEach
    void afterEach() {
//        sut.shutdown();
    }

    @RepeatedTest(10)
    void automationTest() {
        // given
        var eventGateway = sut.getComponent(EventGateway.class);
        RecordingNotificationService notificationService = (RecordingNotificationService) sut.getComponent(NotificationService.class);

        // when
        var studentId1 = StudentId.random();
        var studentId2 = StudentId.random();
        var courseId1 = CourseId.random();
        var courseId2 = CourseId.random();
        List<Object> events = List.of(
                new CourseCreated(courseId1, "Course 1", 2),
                new CourseCreated(courseId2, "Course 1", 2),
                new StudentSubscribedToCourse(studentId1, courseId1),
                new StudentSubscribedToCourse(studentId2, courseId1),
                new StudentSubscribedToCourse(studentId1, courseId2),
                new StudentSubscribedToCourse(studentId2, courseId2)
        );
        eventsOccurred(events);

        // then
        var expectedNotification = new NotificationService.Notification("admin", "All courses are fully booked now.");
        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(notificationService.sent()).contains(expectedNotification));
    }

    protected void eventsOccurred(List<Object> events) {
        var eventGateway = sut.getComponent(EventGateway.class);
        var unitOfWork = sut.getComponent(UnitOfWorkFactory.class).create();
        unitOfWork.onInvocation(ctx -> eventGateway.publish(null, events));
        unitOfWork.execute().join();
//        var eventStore = (RecordingEventStore) sut.getComponent(EventStore.class);
//        eventStore.reset();
    }

}
