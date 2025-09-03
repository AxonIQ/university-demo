package io.axoniq.demo.university.faculty.write.createcourse;

import io.axoniq.demo.university.UniversityApplicationTest;
import io.axoniq.demo.university.faculty.events.CourseCreated;
import io.axoniq.demo.university.shared.ids.CourseId;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.junit.jupiter.api.Test;

public class CreateCourseTest extends UniversityApplicationTest {

    @Override
    protected EventSourcingConfigurer overrideConfigurer(EventSourcingConfigurer configurer) {
        return CreateCourseConfiguration.configure(configurer);
    }

    @Test
    void givenNotExistingCourse_WhenCreateCourse_ThenSuccess() {
        // given
        var courseId = CourseId.random();
        var courseName = "Event Sourcing in Practice";
        var capacity = 3;

        // when
        executeCommand(
                new CreateCourse(courseId, courseName, capacity)
        );

        // then
        assertEvents(
                new CourseCreated(courseId, courseName, capacity)
        );
    }

    @Test
    void givenCourseCreated_WhenCreateCourse_ThenSuccess_NoEvents() {
        // given
        var courseId = CourseId.random();
        var courseName = "Event Sourcing in Practice";
        var capacity = 3;

        eventOccurred(
                new CourseCreated(courseId, courseName, capacity)
        );

        // when
        executeCommand(
                new CreateCourse(courseId, courseName, capacity)
        );

        // then
        assertNoEvents();
    }

}
