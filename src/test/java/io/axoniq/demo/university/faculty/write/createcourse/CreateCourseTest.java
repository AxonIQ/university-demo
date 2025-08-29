package io.axoniq.demo.university.faculty.write.createcourse;

import io.axoniq.demo.university.UniversityAxonApplication;
import io.axoniq.demo.university.faculty.FacultyTestFixture;
import io.axoniq.demo.university.faculty.events.CourseCreated;
import io.axoniq.demo.university.shared.ids.CourseId;
import org.axonframework.common.ReflectionUtils;
import org.axonframework.configuration.ApplicationConfigurer;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventstreaming.StreamableEventSource;
import org.axonframework.test.fixture.AxonTestFixture;
import org.axonframework.test.fixture.RecordingEventStore;
import org.junit.jupiter.api.*;

class CreateCourseTest {

    private AxonTestFixture fixture;

    @BeforeEach
    void beforeEach() {
        fixture = FacultyTestFixture.create();
    }

    @Test
    void givenNotExistingCourse_WhenCreateCourse_ThenSuccess() {
        var courseId = CourseId.random();
        var courseName = "Event Sourcing in Practice";
        var capacity = 3;

        fixture.given()
                .when()
                .command(new CreateCourse(courseId, courseName, capacity))
                .then()
                .success()
                .events(new CourseCreated(courseId, courseName, capacity));
    }

    @Test
    void givenCourseCreated_WhenCreateCourse_ThenSuccess_NoEvents() {
        var courseId = CourseId.random();
        var courseName = "Event Sourcing in Practice";
        var capacity = 3;

        fixture.given()
                .event(new CourseCreated(courseId, courseName, capacity))
                .when()
                .command(new CreateCourse(courseId, courseName, capacity))
                .then()
                .success()
                .noEvents();
    }
}
