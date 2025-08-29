package io.axoniq.demo.university.faculty.automation.studentsubscribednotifier;

public interface NotificationService {

    record Notification(String recipientId, String message) {
    }

    void sendNotification(Notification notification);

}
