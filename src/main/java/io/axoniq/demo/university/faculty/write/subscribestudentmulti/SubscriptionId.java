package io.axoniq.demo.university.faculty.write.subscribestudentmulti;

import io.axoniq.demo.university.faculty.write.CourseId;
import io.axoniq.demo.university.faculty.write.StudentId;

record SubscriptionId(CourseId courseId, StudentId studentId) {

}
