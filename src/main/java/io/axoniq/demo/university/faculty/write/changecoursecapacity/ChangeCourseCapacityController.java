package io.axoniq.demo.university.faculty.write.changecoursecapacity;

import io.axoniq.demo.university.faculty.write.renamecourse.RenameCourse;
import io.axoniq.demo.university.shared.ids.CourseId;
import org.axonframework.commandhandling.gateway.CommandGateway;
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
public class ChangeCourseCapacityController {

  private static final Logger logger = Logger.getLogger(ChangeCourseCapacityController.class.getName());

  private final CommandGateway commandGateway;

  public ChangeCourseCapacityController(CommandGateway commandGateway) {
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
   * curl -X PUT http://localhost:8080/api/courses/my-course-123/capacity \
   * -H "Content-Type: application/json" \
   * -d '{"name":"Event Sourcing 101"}'
   */
  @PutMapping("/{courseId}/capacity")
  public ResponseEntity<Void> renameCourse(@PathVariable("courseId") String courseId, @RequestBody ChangeCourseCapacityRequest request) {
    logger.info("Changing course capacity to: " + request.capacity());

    try {
      ChangeCourseCapacity command = new ChangeCourseCapacity(new CourseId(courseId), request.capacity);

      commandGateway.sendAndWait(command);

      logger.info("Successfully changed capacity of course with ID: " + courseId);

      return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();

    } catch (Exception e) {
      logger.severe("Failed to change course capacity: " + e.getMessage());
      return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .build();
    }
  }

  /**
   * Request DTO for changing capacity of a course.
   *
   * @param capacity The capacity of the course (required)
   */
  public record ChangeCourseCapacityRequest(
    Integer capacity
  ) {
  }

}