package io.axoniq.demo.university.faculty.automation.studentsubscribednotifier;

import io.axoniq.demo.university.faculty.automation.studentsubscribednotifier.infrastructure.LoggingNotificationService;
import io.axoniq.demo.university.faculty.automation.studentsubscribednotifier.infrastructure.RecordingNotificationService;
import org.axonframework.eventhandling.GlobalSequenceTrackingToken;
import org.axonframework.eventhandling.configuration.EventProcessorModule;
import org.axonframework.eventhandling.pooled.PooledStreamingEventProcessorModule;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;

import java.util.concurrent.CompletableFuture;

public class StudentSubscribedNotifierConfiguration {

    public static EventSourcingConfigurer configure(EventSourcingConfigurer configurer) {
        PooledStreamingEventProcessorModule automationProcessor = EventProcessorModule
                .pooledStreaming("Automation_WhenStudentSubscribedThenSendNotification_Processor")
                .eventHandlingComponents(
                        c -> c.annotated(cfg -> new WhenStudentSubscribedThenSendNotification(cfg.getComponent(NotificationService.class)))
                )
                // Due to the InMemoryEventStore bug the customization is needed if you want to use the implementation in the tests
                .customized((c, cus) -> cus.initialToken(s -> CompletableFuture.completedFuture(new GlobalSequenceTrackingToken(0))));

        return configurer
                .componentRegistry(cr -> cr.registerComponent(NotificationService.class, cfg -> new RecordingNotificationService(new LoggingNotificationService())))
                .modelling(modelling -> modelling.messaging(messaging -> messaging.eventProcessing(eventProcessing ->
                        eventProcessing.pooledStreaming(ps -> ps.processor(automationProcessor))
                )));
    }

}
