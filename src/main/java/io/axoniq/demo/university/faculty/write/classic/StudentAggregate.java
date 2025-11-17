package io.axoniq.demo.university.faculty.write.classic;

import io.axoniq.demo.university.faculty.FacultyTags;
import io.axoniq.demo.university.faculty.Ids;
import io.axoniq.demo.university.faculty.events.StudentEnrolledInFaculty;
import io.axoniq.demo.university.shared.ids.FacultyId;
import io.axoniq.demo.university.shared.ids.StudentId;
import org.axonframework.messaging.commandhandling.annotation.CommandHandler;
import org.axonframework.messaging.eventhandling.gateway.EventAppender;
import org.axonframework.eventsourcing.annotation.EventSourcingHandler;
import org.axonframework.eventsourcing.annotation.reflection.EntityCreator;
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
        Ids.FACULTY_ID,
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
