package io.axoniq.demo.university.faculty.automation.studentsubscribednotifierplain;

import io.axoniq.demo.university.faculty.events.StudentSubscribedToCourse;
import io.axoniq.demo.university.shared.ids.StudentId;
import org.axonframework.messaging.core.QualifiedName;
import org.axonframework.messaging.eventhandling.SimpleEventHandlingComponent;
import org.axonframework.messaging.eventhandling.configuration.EventProcessorModule;
import org.axonframework.messaging.eventhandling.processing.streaming.pooled.PooledStreamingEventProcessorModule;
import org.axonframework.messaging.eventhandling.replay.ReplayBlockingEventHandlingComponent;
import org.axonframework.messaging.eventhandling.sequencing.PropertySequencingPolicy;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.jetbrains.annotations.NotNull;

public class StudentSubscribedNotifierConfiguration {

    public static EventSourcingConfigurer configure(EventSourcingConfigurer configurer) {
        PooledStreamingEventProcessorModule automationProcessor = EventProcessorModule
                .pooledStreaming("Automation_WhenStudentSubscribedThenSendNotification_Processor")
                .eventHandlingComponents(
                        c -> c.declarative(cfg -> eventHandlingComponent().subscribe(
                                        new QualifiedName(StudentSubscribedToCourse.class), WhenStudentSubscribedThenSendNotification::react
                                )
                        )
                ).notCustomized();

        return configurer
                .modelling(modelling -> modelling.messaging(messaging -> messaging.eventProcessing(eventProcessing ->
                        eventProcessing.pooledStreaming(ps -> ps.processor(automationProcessor))
                )));
    }

    private static ReplayBlockingEventHandlingComponent eventHandlingComponent() {
        return new ReplayBlockingEventHandlingComponent(
                new SimpleEventHandlingComponent(
                        new PropertySequencingPolicy<StudentSubscribedToCourse, StudentId>(
                                StudentSubscribedToCourse.class,
                                "studentId"
                        )
                )
        );
    }

}
