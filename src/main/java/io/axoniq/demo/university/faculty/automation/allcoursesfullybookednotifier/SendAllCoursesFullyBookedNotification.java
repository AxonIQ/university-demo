package io.axoniq.demo.university.faculty.automation.allcoursesfullybookednotifier;

import io.axoniq.demo.university.shared.ids.FacultyId;

public record SendAllCoursesFullyBookedNotification(FacultyId facultyId) {

}