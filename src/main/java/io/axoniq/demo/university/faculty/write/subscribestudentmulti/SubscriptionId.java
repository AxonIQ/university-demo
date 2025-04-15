package io.axoniq.demo.university.faculty.write.subscribestudentmulti;

import io.axoniq.demo.university.shared.ids.CourseId;
import io.axoniq.demo.university.shared.ids.StudentId;

record SubscriptionId(CourseId courseId, StudentId studentId) {

}
