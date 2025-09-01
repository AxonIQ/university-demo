package io.axoniq.demo.university.faculty.automation.studentsubscribednotifier;

import io.axoniq.demo.university.faculty.events.StudentSubscribedToCourse;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.messaging.MessageStream;

/**
 * Automation that reacts on {@link StudentSubscribedToCourse} events and sends a notification.
 * This Event Handler is stateless (so no need to validate some state) and always executes the same action.
 */
public class WhenStudentSubscribedThenSendNotification {

    private final NotificationService notificationService;

    public WhenStudentSubscribedThenSendNotification(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventHandler
    MessageStream.Empty<?> react(StudentSubscribedToCourse event) {
        var notification = new NotificationService.Notification(
                event.studentId().toString(),
                "You have subscribed to course " + event.courseId()
        );
        notificationService.sendNotification(notification);
        return MessageStream.empty();
    }

}

// todo: automation - when all courses