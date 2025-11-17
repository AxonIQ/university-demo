package io.axoniq.demo.university.faculty.write.renamecourse;

import io.axoniq.demo.university.shared.ids.CourseId;
import org.axonframework.messaging.commandhandling.gateway.CommandGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;

/**
 * REST Controller for renaming courses.
 * <p>
 * This controller handles course renaming requests and dispatches them via Axon CommandGateway.
 */
@RestController
@RequestMapping("/api/courses")
public class RenameCourseController {

  private static final Logger logger = Logger.getLogger(RenameCourseController.class.getName());

  private final CommandGateway commandGateway;

  public RenameCourseController(CommandGateway commandGateway) {
    this.commandGateway = commandGateway;
  }

  /**
   * Endpoint for renaming an existing course.
   * <p>
   * Usage: POST /api/courses/{courseId}
   * <p>
   * Request body:
   * {
   * "name": "Course Name"
   * }
   * <p>
   * Example with curl:
   * curl -X PUT http://localhost:8080/api/courses/my-course-123/rename \
   * -H "Content-Type: application/json" \
   * -d '{"name":"Event Sourcing 101"}'
   */
  @PutMapping("/{courseId}/rename")
  public ResponseEntity<Void> renameCourse(@PathVariable("courseId") String courseId, @RequestBody RenameCourseRequest request) {
    logger.info("Renaming course: " + request.name() + " to " + request.name);

    try {
      RenameCourse command = new RenameCourse(new CourseId(courseId), request.name());

      commandGateway.sendAndWait(command);

      logger.info("Successfully renamed course with ID: " + courseId);

      return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();

    } catch (Exception e) {
      logger.severe("Failed to rename course: " + e.getMessage());
      return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .build();
    }
  }

  /**
   * Request DTO for renaming a course.
   *
   * @param name The name of the course (required)
   */
  public record RenameCourseRequest(
    String name
  ) {
  }

}