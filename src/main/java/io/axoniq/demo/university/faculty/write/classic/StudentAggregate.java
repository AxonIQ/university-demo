package io.axoniq.demo.university.faculty.write.classic;

import io.axoniq.demo.university.faculty.FacultyTags;
import io.axoniq.demo.university.faculty.events.StudentEnrolledInFaculty;
import io.axoniq.demo.university.shared.ids.StudentId;
import org.axonframework.commandhandling.annotations.CommandHandler;
import org.axonframework.eventhandling.gateway.EventAppender;
import org.axonframework.eventsourcing.annotations.EventSourcingHandler;
import org.axonframework.eventsourcing.annotations.reflection.EntityCreator;
import org.axonframework.extension.spring.stereotype.EventSourced;

@EventSourced(tagKey = FacultyTags.STUDENT_ID, idType = StudentId.class)
public class StudentAggregate {

  private StudentId studentId;

  @EntityCreator
  public StudentAggregate(StudentEnrolledInFaculty enrolledInFaculty) {
    this.studentId = enrolledInFaculty.studentId();
  }

  @CommandHandler
  public static void handle(EnrollStudent command, EventAppender appender) {
    appender.append(
      new StudentEnrolledInFaculty(
        command.studentId(),
        command.firstname(),
        command.lastname()
      )
    );
  }

  @EventSourcingHandler
  public void on(StudentEnrolledInFaculty event) {
    this.studentId = event.studentId();
  }


}
