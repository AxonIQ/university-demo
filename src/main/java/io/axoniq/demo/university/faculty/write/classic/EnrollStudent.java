package io.axoniq.demo.university.faculty.write.classic;

import io.axoniq.demo.university.shared.ids.StudentId;
import org.axonframework.modelling.annotations.TargetEntityId;

public record EnrollStudent(
  @TargetEntityId
  StudentId studentId,
  String firstname,
  String lastname
) {
}
