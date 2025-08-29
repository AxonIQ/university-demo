package io.axoniq.demo.university.faculty.automation.studentsubscribednotifier;

import org.axonframework.eventhandling.configuration.EventProcessorModule;
import org.axonframework.eventhandling.pooled.PooledStreamingEventProcessorModule;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;

public class StudentSubscribedNotifierConfiguration {

    public static EventSourcingConfigurer configure(EventSourcingConfigurer configurer) {
        PooledStreamingEventProcessorModule automationProcessor = EventProcessorModule
                .pooledStreaming("Automation_WhenStudentSubscribedThenSendNotification_Processor")
                .eventHandlingComponents(
                        c -> c.annotated(cfg -> new WhenStudentSubscribedThenSendNotification(cfg.getComponent(NotificationService.class)))
                )
                .notCustomized();

        return configurer
                .componentRegistry(cr -> cr.registerComponent(NotificationService.class, cfg -> new LoggingNotificationService()))
                .modelling(modelling -> modelling.messaging(messaging -> messaging.eventProcessing(eventProcessing ->
                        eventProcessing.pooledStreaming(ps -> ps.processor(automationProcessor))
                )));
    }

}
