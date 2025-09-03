package io.axoniq.demo.university.faculty.automation.studentsubscribednotifier;

import io.axoniq.demo.university.UniversityApplicationTest;
import io.axoniq.demo.university.shared.infrastructure.notifier.RecordingNotificationService;
import io.axoniq.demo.university.faculty.events.StudentSubscribedToCourse;
import io.axoniq.demo.university.shared.application.notifier.NotificationService;
import io.axoniq.demo.university.shared.ids.CourseId;
import io.axoniq.demo.university.shared.ids.StudentId;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;

public class WhenStudentSubscribedThenSendNotificationTest extends UniversityApplicationTest {

    @Test
    void automationTest() {
        // given
        var eventGateway = sut.getComponent(EventGateway.class);
        RecordingNotificationService notificationService = (RecordingNotificationService) sut.getComponent(NotificationService.class);

        // when
        var studentId = StudentId.random();
        var courseId = CourseId.random();
        eventsOccurred(new StudentSubscribedToCourse(studentId, courseId));

        // then
        var expectedNotification = new NotificationService.Notification(studentId.raw(), "You have subscribed to course " + courseId);
        await().atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(notificationService.sent()).contains(expectedNotification));
    }

}
