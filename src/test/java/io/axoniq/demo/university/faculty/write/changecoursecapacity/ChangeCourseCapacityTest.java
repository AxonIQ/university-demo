package io.axoniq.demo.university.faculty.write.changecoursecapacity;

import io.axoniq.demo.university.UniversityApplicationTest;
import io.axoniq.demo.university.faculty.events.CourseCapacityChanged;
import io.axoniq.demo.university.faculty.events.CourseCreated;
import io.axoniq.demo.university.faculty.events.CourseRenamed;
import io.axoniq.demo.university.shared.ids.CourseId;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ChangeCourseCapacityTest extends UniversityApplicationTest {

    @Override
    protected EventSourcingConfigurer overrideConfigurer(EventSourcingConfigurer configurer) {
        return ChangeCourseCapacityConfiguration.configure(configurer);
    }

    @Test
    void givenNotExistingCourse_WhenChangeCapacity_ThenException() {
        // given
        var courseId = CourseId.random();

        // when-then
        assertThatThrownBy(() -> executeCommand(
                new ChangeCourseCapacity(courseId, 5)
        )).cause().hasMessageContaining("Course with given id does not exist");
    }

    @RepeatedTest(10)
    void givenCourseCreated_WhenChangeCapacity_ThenSuccess() {
        // given
        var courseId = CourseId.random();
        eventOccurred(
                new CourseCreated(courseId, "Event Sourcing in Practice", 42)
        );

        // when
        executeCommand(
                new ChangeCourseCapacity(courseId, 7)
        );

        // then
        assertEvents(
                new CourseCapacityChanged(courseId, 7)
        );
    }

    @Test
    void givenCourseCreated_WhenChangeCapacityToTheSameName_ThenSuccess_NoEvents() {
        // given
        var courseId = CourseId.random();
        eventOccurred(
                new CourseCreated(courseId, "Event Sourcing in Practice", 42)
        );

        // when
        executeCommand(
                new ChangeCourseCapacity(courseId, 42)
        );

        // then
        assertNoEvents();
    }

    @Test
    void givenCourseCreatedAndRenamed_WhenChangeCapacity_ThenSuccess() {
        // given
        var courseId = CourseId.random();
        eventsOccurred(
                new CourseCreated(courseId, "Event Sourcing in Practice", 42),
                new CourseRenamed(courseId, "Event Sourcing in Theory")
        );

        // when
        executeCommand(
                new ChangeCourseCapacity(courseId, 7)
        );

        // then
        assertEvents(
                new CourseCapacityChanged(courseId, 7)
        );
    }

}
