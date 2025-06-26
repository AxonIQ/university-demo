package io.axoniq.demo.university.faculty.infrastructure;

import io.axoniq.demo.university.faculty.StringFacultyEventConverter;
import io.axoniq.demo.university.faculty.events.CourseCreated;
import io.axoniq.demo.university.faculty.events.StudentEnrolledInFaculty;
import io.axoniq.demo.university.faculty.events.StudentSubscribedToCourse;
import io.axoniq.demo.university.shared.ids.CourseId;
import io.axoniq.demo.university.shared.ids.StudentId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StringFacultyEventConverterTest {

    private StringFacultyEventConverter converter;

    @BeforeEach
    void setUp() {
        converter = new StringFacultyEventConverter();
    }

    @Test
    void shouldConvertCourseCreatedToStringAndBack() {
        // given
        CourseCreated original = new CourseCreated(CourseId.of("MATH101"), "Mathematics", 30);

        // when
        String serialized = converter.convert(original, String.class);
        CourseCreated deserialized = converter.convert(serialized, CourseCreated.class);

        // then
        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void shouldConvertCourseCreatedToByteArrayAndBack() {
        // given
        CourseCreated original = new CourseCreated(CourseId.of("PHYS101"), "Physics", 25);

        // when
        byte[] serialized = converter.convert(original, byte[].class);
        CourseCreated deserialized = converter.convert(serialized, CourseCreated.class);

        // then
        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void shouldConvertStudentEnrolledInFacultyToStringAndBack() {
        // given
        StudentEnrolledInFaculty original = new StudentEnrolledInFaculty(
                StudentId.of("STU001"), "John", "Doe"
        );

        // when
        String serialized = converter.convert(original, String.class);
        StudentEnrolledInFaculty deserialized = converter.convert(serialized, StudentEnrolledInFaculty.class);

        // then
        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void shouldConvertStudentSubscribedToCourseToStringAndBack() {
        // given
        StudentSubscribedToCourse original = new StudentSubscribedToCourse(
                StudentId.of("STU001"), CourseId.of("MATH101")
        );

        // when
        String serialized = converter.convert(original, String.class);
        StudentSubscribedToCourse deserialized = converter.convert(serialized, StudentSubscribedToCourse.class);

        // then
        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void shouldReturnTrueForSupportedConversions() {
        assertThat(converter.canConvert(CourseCreated.class, String.class)).isTrue();
        assertThat(converter.canConvert(CourseCreated.class, byte[].class)).isTrue();
        assertThat(converter.canConvert(String.class, CourseCreated.class)).isTrue();
        assertThat(converter.canConvert(byte[].class, CourseCreated.class)).isTrue();

        assertThat(converter.canConvert(StudentEnrolledInFaculty.class, String.class)).isTrue();
        assertThat(converter.canConvert(StudentSubscribedToCourse.class, byte[].class)).isTrue();
    }

    @Test
    void shouldReturnFalseForUnsupportedConversions() {
        assertThat(converter.canConvert(String.class, Integer.class)).isFalse();
        assertThat(converter.canConvert(Object.class, String.class)).isFalse();
        assertThat(converter.canConvert(CourseCreated.class, Integer.class)).isFalse();
    }
}