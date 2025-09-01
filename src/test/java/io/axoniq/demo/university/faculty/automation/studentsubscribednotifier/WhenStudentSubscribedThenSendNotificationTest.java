package io.axoniq.demo.university.faculty.automation.studentsubscribednotifier;

import io.axoniq.demo.university.UniversityAxonApplication;
import io.axoniq.demo.university.faculty.automation.studentsubscribednotifier.infrastructure.RecordingNotificationService;
import io.axoniq.demo.university.faculty.events.StudentSubscribedToCourse;
import io.axoniq.demo.university.shared.ids.CourseId;
import io.axoniq.demo.university.shared.ids.StudentId;
import org.awaitility.Awaitility;
import org.axonframework.configuration.AxonConfiguration;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

public class WhenStudentSubscribedThenSendNotificationTest {

    private AxonConfiguration sut;

    @BeforeEach
    void beforeEach() {
        var application = new UniversityAxonApplication();
        sut = application.configurer().start();
    }

    @AfterEach
    void afterEach() {
        sut.shutdown();
    }

    @Test
    void test() {
        // given
        var eventGateway = sut.getComponent(EventGateway.class);
        RecordingNotificationService notificationService = (RecordingNotificationService) sut.getComponent(NotificationService.class);

        // when
        var studentId = StudentId.random();
        var courseId = CourseId.random();
        eventGateway.publish(null, new StudentSubscribedToCourse(studentId, courseId));

        // then
        var expectedNotification = new NotificationService.Notification(studentId.raw(), "You have subscribed to course " + courseId);
        Awaitility.await()
                .atMost(2, TimeUnit.SECONDS)
                .until(() -> notificationService.sent().contains(expectedNotification));
    }


}
