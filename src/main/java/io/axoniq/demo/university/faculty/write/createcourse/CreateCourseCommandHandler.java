package io.axoniq.demo.university.faculty.write.createcourse;

import io.axoniq.demo.university.faculty.FacultyTags;
import io.axoniq.demo.university.faculty.Ids;
import io.axoniq.demo.university.faculty.events.CourseCreated;
import io.axoniq.demo.university.shared.ids.CourseId;
import org.axonframework.messaging.commandhandling.annotation.CommandHandler;
import org.axonframework.messaging.eventhandling.gateway.EventAppender;
import org.axonframework.eventsourcing.annotation.EventSourcingHandler;
import org.axonframework.eventsourcing.annotation.reflection.EntityCreator;
import org.axonframework.extension.spring.stereotype.EventSourced;
import org.axonframework.modelling.annotation.InjectEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class CreateCourseCommandHandler {

  @CommandHandler
  void handle(
    CreateCourse command,
    @InjectEntity(idProperty = FacultyTags.COURSE_ID) State state,
    EventAppender eventAppender
  ) {
    var events = decide(command, state);
    eventAppender.append(events);
  }

  private List<CourseCreated> decide(CreateCourse command, State state) {
    if (state.created) {
      return List.of();
    }
    return List.of(new CourseCreated(Ids.FACULTY_ID, command.courseId(), command.name(), command.capacity()));
  }

  @EventSourced(idType = CourseId.class, tagKey = FacultyTags.COURSE_ID)
  static final class State {

    private boolean created;

    @EntityCreator
    private State() {
      this.created = false;
    }

    @EventSourcingHandler
    private State apply(CourseCreated event) {
      this.created = true;
      return this;
    }
  }
}
