package io.axoniq.demo.university.faculty.automation.studentsubscribednotifier;

import io.axoniq.demo.university.UniversityAxonApplication;
import io.axoniq.demo.university.faculty.automation.studentsubscribednotifier.infrastructure.RecordingNotificationService;
import io.axoniq.demo.university.faculty.events.StudentSubscribedToCourse;
import io.axoniq.demo.university.shared.ids.CourseId;
import io.axoniq.demo.university.shared.ids.StudentId;
import org.axonframework.configuration.AxonConfiguration;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;

public class WhenStudentSubscribedThenSendNotificationTest {

    private AxonConfiguration sut;

    @BeforeEach
    void beforeEach() {
        sut = UniversityAxonApplication.startApplication();
    }

    @AfterEach
    void afterEach() {
        sut.shutdown();
    }

    // TODO: why does it take 18 seconds with InMemory, but 2 with AxonServer !!!???
    @Test
    void automationTest() {
        // given
        var eventGateway = sut.getComponent(EventGateway.class);
        RecordingNotificationService notificationService = (RecordingNotificationService) sut.getComponent(NotificationService.class);

        // when
        var studentId = StudentId.random();
        var courseId = CourseId.random();
        eventGateway.publish(null, new StudentSubscribedToCourse(studentId, courseId));

        // then
        var expectedNotification = new NotificationService.Notification(studentId.raw(), "You have subscribed to course " + courseId);
        await().atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(notificationService.sent()).contains(expectedNotification));
    }


}
