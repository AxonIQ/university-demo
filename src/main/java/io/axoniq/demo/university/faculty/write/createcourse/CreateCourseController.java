package io.axoniq.demo.university.faculty.write.createcourse;

import io.axoniq.demo.university.shared.ids.CourseId;
import org.axonframework.messaging.commandhandling.gateway.CommandGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;

/**
 * REST Controller for creating courses.
 * <p>
 * This controller handles course creation requests and dispatches them via Axon CommandGateway.
 */
@RestController
@RequestMapping("/api/courses")
public class CreateCourseController {

    private static final Logger logger = Logger.getLogger(CreateCourseController.class.getName());

    private final CommandGateway commandGateway;

    public CreateCourseController(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    /**
     * Endpoint for creating a new course.
     * <p>
     * Usage: POST /api/courses
     * <p>
     * Request body:
     * {
     *   "courseId": "course-id-string",
     *   "name": "Course Name",
     *   "capacity": 30
     * }
     * <p>
     * Example with curl:
     * curl -X POST http://localhost:8080/api/courses \
     *   -H "Content-Type: application/json" \
     *   -d '{"courseId":"my-course-123","name":"Event Sourcing 101","capacity":25}'
     */
    @PostMapping
    public ResponseEntity<CreateCourseResponse> createCourse(@RequestBody CreateCourseRequest request) {
        logger.info("Creating course: " + request.name() + " with capacity: " + request.capacity());

        try {
            CourseId courseId = request.courseId() != null && !request.courseId().isEmpty()
                    ? new CourseId(request.courseId())
                    : CourseId.random();

            CreateCourse command = new CreateCourse(courseId, request.name(), request.capacity());

            commandGateway.sendAndWait(command);

            logger.info("Successfully created course with ID: " + courseId.raw());

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new CreateCourseResponse(courseId.raw(), request.name(), request.capacity()));

        } catch (Exception e) {
            logger.severe("Failed to create course: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    /**
     * Request DTO for creating a course.
     *
     * @param courseId Optional course ID. If not provided, a random ID will be generated.
     * @param name The name of the course (required)
     * @param capacity The maximum number of students (required)
     */
    public record CreateCourseRequest(
            String courseId,
            String name,
            int capacity
    ) {}

    /**
     * Response DTO after successfully creating a course.
     *
     * @param courseId The ID of the created course
     * @param name The name of the course
     * @param capacity The capacity of the course
     */
    public record CreateCourseResponse(
            String courseId,
            String name,
            int capacity
    ) {}
}