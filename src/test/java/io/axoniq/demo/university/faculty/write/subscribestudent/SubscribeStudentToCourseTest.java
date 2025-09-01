package io.axoniq.demo.university.faculty.write.subscribestudent;

import io.axoniq.demo.university.faculty.FacultyTestFixture;
import io.axoniq.demo.university.faculty.events.CourseCreated;
import io.axoniq.demo.university.faculty.events.CourseFullyBooked;
import io.axoniq.demo.university.faculty.events.StudentEnrolledInFaculty;
import io.axoniq.demo.university.faculty.events.StudentSubscribedToCourse;
import io.axoniq.demo.university.shared.ids.CourseId;
import io.axoniq.demo.university.shared.ids.StudentId;
import org.axonframework.test.fixture.AxonTestFixture;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

class SubscribeStudentToCourseTest {

    private AxonTestFixture fixture;

    @BeforeEach
    void beforeEach() {
        fixture = FacultyTestFixture.create();
    }

    @Test
    void successfulSubscription() {
        var courseId = CourseId.random();
        var studentId = StudentId.random();

        fixture.given()
               .event(new StudentEnrolledInFaculty(studentId, "Mateusz", "Nowak"))
               .event(new CourseCreated(courseId, "Axon Framework 5: Be a PRO", 2))
               .when()
               .command(new SubscribeStudentToCourse(studentId, courseId))
               .then()
               .events(new StudentSubscribedToCourse(studentId, courseId));
    }

    @Test
    void studentAlreadySubscribed() {
        var courseId = CourseId.random();
        var studentId = StudentId.random();

        fixture.given()
               .event(new StudentEnrolledInFaculty(studentId, "Allard", "Buijze"))
               .event(new CourseCreated(courseId, "Axon Framework 5: Be a PRO", 2))
               .event(new StudentSubscribedToCourse(studentId, courseId))
               .when()
               .command(new SubscribeStudentToCourse(studentId, courseId))
               .then()
               .exception(RuntimeException.class, "Student already subscribed to this course");
    }

    @Test
    void studentAlreadySubscribedAnotherCourse() {
        var courseId = CourseId.random();
        var anotherCourseId = CourseId.random();
        var studentId = StudentId.random();

        fixture.given()
                .event(new StudentEnrolledInFaculty(studentId, "Allard", "Buijze"))
                .event(new CourseCreated(courseId, "Axon Framework 5: Be a PRO", 2))
                .event(new StudentSubscribedToCourse(studentId, anotherCourseId))
                .when()
                .command(new SubscribeStudentToCourse(studentId, courseId))
                .then()
                .events(new StudentSubscribedToCourse(studentId, courseId));
    }

    @Test
    void studentSubscribedIfAnotherOneAlreadySubscribed() {
        var courseId = CourseId.random();
        var studentId = StudentId.random();
        var anotherStudentId = StudentId.random();

        fixture.given()
                .event(new StudentEnrolledInFaculty(studentId, "Allard", "Buijze"))
                .event(new StudentEnrolledInFaculty(anotherStudentId, "Marc", "Gathier"))
                .event(new CourseCreated(courseId, "Axon Framework 5: Be a PRO", 2))
                .event(new StudentSubscribedToCourse(anotherStudentId, courseId))
                .when()
                .command(new SubscribeStudentToCourse(studentId, courseId))
                .then()
                .events(new StudentSubscribedToCourse(studentId, courseId));
    }

    @Test
    void courseFullyBooked() {
        var courseId = CourseId.random();
        var student1Id = StudentId.random();
        var student2Id = StudentId.random();
        var student3Id = StudentId.random();

        fixture.given()
               .event(new StudentEnrolledInFaculty(student1Id, "Mateusz", "Nowak"))
               .event(new StudentEnrolledInFaculty(student2Id, "Steven", "van Beelen"))
               .event(new StudentEnrolledInFaculty(student3Id, "Mitchell", "Herrijgers"))
               .event(new CourseCreated(courseId, "Event Sourcing Masterclass", 2))
               .event(new StudentSubscribedToCourse(student1Id, courseId))
               .event(new StudentSubscribedToCourse(student2Id, courseId))
               .when()
               .command(new SubscribeStudentToCourse(student3Id, courseId))
               .then()
               .exception(RuntimeException.class, "Course is fully booked");
    }

    @Test
    void courseFullyBookedWhenLastSpotTaken() {
        var courseId = CourseId.random();
        var student1Id = StudentId.random();
        var student2Id = StudentId.random();

        fixture.given()
               .event(new StudentEnrolledInFaculty(student1Id, "Mateusz", "Nowak"))
               .event(new StudentEnrolledInFaculty(student2Id, "Steven", "van Beelen"))
               .event(new CourseCreated(courseId, "Event Sourcing Masterclass", 2))
               .event(new StudentSubscribedToCourse(student1Id, courseId))
               .when()
               .command(new SubscribeStudentToCourse(student2Id, courseId))
               .then()
               .events(new StudentSubscribedToCourse(student2Id, courseId), new CourseFullyBooked(courseId));
    }

    @Test
    void studentSubscribedToTooManyCourses() {
        var studentId = StudentId.random();
        var course1Id = CourseId.random();
        var course2Id = CourseId.random();
        var course3Id = CourseId.random();
        var targetCourseId = CourseId.random();

        fixture.given()
                .event(new StudentEnrolledInFaculty(studentId, "Milan", "Savic"))
                .event(new CourseCreated(targetCourseId, "Programming", 10))
                .event(new CourseCreated(course1Id, "Course 1", 10))
                .event(new CourseCreated(course2Id, "Course 2", 10))
                .event(new CourseCreated(course3Id, "Course 3", 10))
                .event(new StudentSubscribedToCourse(studentId, course1Id))
                .event(new StudentSubscribedToCourse(studentId, course2Id))
                .event(new StudentSubscribedToCourse(studentId, course3Id))
                .when()
                .command(new io.axoniq.demo.university.faculty.write.subscribestudentmulti.SubscribeStudentToCourse(studentId, targetCourseId))
                .then()
                .noEvents()
                .exceptionSatisfies(thrown -> assertThat(thrown)
                        .isInstanceOf(RuntimeException.class)
                        .hasMessage("Student subscribed to too many courses")
                );
    }
}
