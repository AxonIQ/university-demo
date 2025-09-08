package io.axoniq.demo.university.faculty.automation.studentsubscribednotifierplain;

import io.axoniq.demo.university.faculty.events.StudentSubscribedToCourse;
import io.axoniq.demo.university.shared.ids.StudentId;
import org.axonframework.eventhandling.SimpleEventHandlingComponent;
import org.axonframework.eventhandling.configuration.EventProcessorModule;
import org.axonframework.eventhandling.processors.streaming.pooled.PooledStreamingEventProcessorModule;
import org.axonframework.eventhandling.processors.streaming.token.GlobalSequenceTrackingToken;
import org.axonframework.eventhandling.sequencing.PropertySequencingPolicy;
import org.axonframework.eventhandling.sequencing.SequentialPolicy;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.axonframework.messaging.QualifiedName;

import java.util.concurrent.CompletableFuture;

public class StudentSubscribedNotifierConfiguration {

    public static EventSourcingConfigurer configure(EventSourcingConfigurer configurer) {
        PooledStreamingEventProcessorModule automationProcessor = EventProcessorModule
                .pooledStreaming("Automation_WhenStudentSubscribedThenSendNotification_Processor")
                .eventHandlingComponents(
                        c -> c.declarative(cfg -> SimpleEventHandlingComponent.builder()
                                .sequencingPolicy(
                                        PropertySequencingPolicy.builder(StudentSubscribedToCourse.class, StudentId.class)
                                                .propertyName("studentId")
                                                .build()
                                )
                                .handles(new QualifiedName(StudentSubscribedToCourse.class), WhenStudentSubscribedThenSendNotification::react).build())
                )
                // Due to a minor bug in the InMemoryEventStorageEngine this customization is needed if you want to use the implementation in the tests
                .customized((c, cus) -> cus.initialToken(s -> CompletableFuture.completedFuture(new GlobalSequenceTrackingToken(0))));

        return configurer
                .modelling(modelling -> modelling.messaging(messaging -> messaging.eventProcessing(eventProcessing ->
                        eventProcessing.pooledStreaming(ps -> ps.processor(automationProcessor))
                )));
    }

}
