package io.axoniq.demo.university.faculty.write.renamecourse;

import io.axoniq.demo.university.shared.ids.CourseId;
import org.axonframework.modelling.annotation.TargetEntityId;

public record RenameCourse(@TargetEntityId CourseId courseId, String name) {

}
