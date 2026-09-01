package io.axoniq.demo.university.faculty.automation.studentsubscribednotifierplain;

import io.axoniq.demo.university.UniversityApplicationTest;
import io.axoniq.demo.university.faculty.Ids;
import io.axoniq.demo.university.faculty.events.StudentSubscribedToCourse;
import io.axoniq.demo.university.shared.application.notifier.NotificationService;
import io.axoniq.demo.university.shared.configuration.NotificationServiceConfiguration;
import io.axoniq.demo.university.shared.ids.CourseId;
import io.axoniq.demo.university.shared.ids.StudentId;
import io.axoniq.demo.university.shared.infrastructure.notifier.RecordingNotificationService;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.axonframework.messaging.eventhandling.processing.streaming.pooled.PooledStreamingEventProcessor;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.axonframework.common.FutureUtils.joinAndUnwrap;

public class WhenStudentSubscribedThenSendNotificationTest extends UniversityApplicationTest {

    @Override
    protected EventSourcingConfigurer overrideConfigurer(EventSourcingConfigurer configurer) {
        configurer = NotificationServiceConfiguration.configure(configurer);
        configurer = StudentSubscribedNotifierConfiguration.configure(configurer);
        return configurer;
    }

    @Test
    void automationTest() {
        // when
        var studentId = StudentId.random();
        var courseId = CourseId.random();
        eventsOccurred(new StudentSubscribedToCourse(Ids.FACULTY_ID, studentId, courseId));

        // then
        var expectedNotification = new NotificationService.Notification(studentId.raw(), "You have subscribed to course " + courseId);
        var notificationService = (RecordingNotificationService) configuration.getComponent(NotificationService.class);
        await().atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(notificationService.sent()).contains(expectedNotification));
    }

    @Test
    void notificationIsNotSendDuringReplay() {
        // given
        var studentId = StudentId.random();
        var courseId = CourseId.random();
        eventsOccurred(new StudentSubscribedToCourse(Ids.FACULTY_ID, studentId, courseId));

        // processor handled the StudentSubscribedToCourse
        var processor = configuration.getComponents(PooledStreamingEventProcessor.class)
                .get("Automation_WhenStudentSubscribedThenSendNotification_Processor");
        await().atMost(1, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(processor.processingStatus().get(0).getCurrentPosition()).hasValue(1));

        // when
        // shutdown the processor when event was processed
        joinAndUnwrap(processor.shutdown());
        var expectedNotification = new NotificationService.Notification(studentId.raw(), "You have subscribed to course " + courseId);
        var notificationService = (RecordingNotificationService) configuration.getComponent(NotificationService.class);
        assertThat(notificationService.sent()).contains(expectedNotification);
        notificationService.clear();

        // reset the event processor and start it again
        joinAndUnwrap(processor.resetTokens());
        joinAndUnwrap(processor.start());

        // then
        // wait until the processor has replayed the event
        await().atMost(1, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(processor.processingStatus().get(0).getCurrentPosition()).hasValue(1));
        // validate that no notification was sent
        assertThat(notificationService.sent()).isEmpty();
    }

}
