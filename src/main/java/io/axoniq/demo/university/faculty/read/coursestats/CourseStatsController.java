package io.axoniq.demo.university.faculty.read.coursestats;

import io.axoniq.demo.university.shared.ids.CourseId;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.logging.Logger;

/**
 * REST Controller demonstrating subscription queries with Server-Sent Events (SSE).
 * <p>
 * This controller streams real-time course statistics updates to clients.
 */
@RestController
@RequestMapping("/api/courses")
public class CourseStatsController {

    private static final Logger logger = Logger.getLogger(CourseStatsController.class.getName());

    private final QueryGateway queryGateway;

    public CourseStatsController(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    /**
     * Endpoint for streaming course stats using Server-Sent Events (SSE).
     * <p>
     * Usage: GET /api/courses/{courseId}/stats/stream
     * <p>
     * The client will receive:
     * 1. An initial event with the current course stats
     * 2. Real-time update events whenever the course stats change
     * <p>
     * Example with curl:
     * curl -N http://localhost:8080/api/courses/{courseId}/stats/stream
     * <p>
     * Example with JavaScript:
     * const eventSource = new EventSource('/api/courses/{courseId}/stats/stream');
     * eventSource.onmessage = (event) => {
     * const stats = JSON.parse(event.data);
     * console.log('Course stats:', stats);
     * };
     */
    @GetMapping(value = "/{courseId}/stats/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<GetCourseStatsById.Result>> streamCourseStats(
            @PathVariable(name = "courseId") String courseId) {

        logger.info("Client subscribed to course stats stream for courseId: " + courseId);

        CourseId id = new CourseId(courseId);
        GetCourseStatsById query = new GetCourseStatsById(id);

        // Create subscription query
        var subscriptionQuery =
                queryGateway.subscriptionQuery(
                        query,
                        GetCourseStatsById.Result.class,
                        null
                );

        return Flux.from(subscriptionQuery)
                .doOnNext(it -> logger.info("Received course stats update: " + it))
                .doOnError(it -> logger.info("Received course stats error: " + it))

                .map(result -> ServerSentEvent.<GetCourseStatsById.Result>builder()
                        .data(result)
                        .event("course-stats-update")
                        .build());
    }

    @GetMapping(value = "/stats", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<GetCourseStatsById.Result> findCoursesStats() {
        return Mono.fromFuture(queryGateway.queryMany(new FindAll(), GetCourseStatsById.Result.class, null))
                .flatMapMany(Flux::fromIterable);
    }

        @GetMapping(value = "/stats/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<GetCourseStatsById.Result>> streamCoursesStats() {

        var subscriptionQuery =
                queryGateway.subscriptionQuery(
                        new FindAll(),
                        GetCourseStatsById.Result.class,
                        null
                );

        return Flux.from(subscriptionQuery)
                .doOnNext(it -> logger.info("Received course stats update: " + it))
                .doOnError(it -> logger.info("Received course stats error: " + it))

                .map(result -> ServerSentEvent.<GetCourseStatsById.Result>builder()
                        .data(result)
                        .event("courses-stats-update")
                        .build());
    }
//
}
