package io.axoniq.demo.university.faculty.read.coursestats;

import org.axonframework.eventhandling.configuration.EventProcessorModule;
import org.axonframework.eventhandling.processors.streaming.pooled.PooledStreamingEventProcessorModule;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;

class CourseStatsConfiguration {

    public static EventSourcingConfigurer configure(EventSourcingConfigurer configurer) {
        PooledStreamingEventProcessorModule projectionProcessor = EventProcessorModule
                .pooledStreaming("Projection_CourseStats_Processor")
                .eventHandlingComponents(
                        c -> c.annotated(cfg -> new CoursesStatsProjection(cfg.getComponent(CourseStatsRepository.class)))
                ).notCustomized();

        return configurer
                .componentRegistry(cr -> cr.registerComponent(CourseStatsRepository.class, cfg -> new InMemoryCourseStatsRepository()))
                .componentRegistry(cr -> cr.registerComponent(GetCourseStatsByIdQueryHandler.class, cfg -> new GetCourseStatsByIdQueryHandler(cfg.getComponent(CourseStatsRepository.class))))
                .modelling(modelling -> modelling.messaging(messaging -> messaging.eventProcessing(eventProcessing ->
                        eventProcessing.pooledStreaming(ps -> ps.processor(projectionProcessor))
                )));
    }

    private CourseStatsConfiguration() {
        // Prevent instantiation
    }

}
