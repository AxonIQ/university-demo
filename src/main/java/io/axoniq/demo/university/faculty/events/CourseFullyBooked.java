package io.axoniq.demo.university.faculty.events;

import io.axoniq.demo.university.faculty.FacultyTags;
import io.axoniq.demo.university.shared.ids.CourseId;
import org.axonframework.eventsourcing.annotations.EventTag;

public record CourseFullyBooked(
        @EventTag(key = FacultyTags.COURSE_ID)
        CourseId courseId
) {

}