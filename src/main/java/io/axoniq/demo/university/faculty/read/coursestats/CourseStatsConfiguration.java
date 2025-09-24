package io.axoniq.demo.university.faculty.read.coursestats;

import org.axonframework.eventhandling.configuration.EventProcessorModule;
import org.axonframework.eventhandling.processors.streaming.pooled.PooledStreamingEventProcessorModule;
import org.axonframework.eventhandling.processors.streaming.token.GlobalSequenceTrackingToken;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;

import java.util.concurrent.CompletableFuture;

class CourseStatsConfiguration {

    public static EventSourcingConfigurer configure(EventSourcingConfigurer configurer) {
        PooledStreamingEventProcessorModule projectionProcessor = EventProcessorModule
                .pooledStreaming("Projection_CourseStats_Processor")
                .eventHandlingComponents(
                        c -> c.annotated(cfg -> new CoursesStatsProjection(cfg.getComponent(CourseStatsRepository.class)))
                ).notCustomized();

        return configurer
                .componentRegistry(cr -> cr.registerComponent(CourseStatsRepository.class, cfg -> new InMemoryCourseStatsRepository()))
                .modelling(modelling -> modelling.messaging(messaging -> messaging.eventProcessing(eventProcessing ->
                        eventProcessing.pooledStreaming(ps -> ps.processor(projectionProcessor))
                )));
    }

    private CourseStatsConfiguration() {
        // Prevent instantiation
    }

}
