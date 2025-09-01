package io.axoniq.demo.university.faculty.automation.allcoursesfullybookednotifier;

import io.axoniq.demo.university.faculty.FacultyTags;
import io.axoniq.demo.university.faculty.automation.studentsubscribednotifier.NotificationService;
import io.axoniq.demo.university.faculty.events.*;
import io.axoniq.demo.university.shared.ids.CourseId;
import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventhandling.gateway.EventAppender;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.eventsourcing.annotation.EventSourcedEntity;
import org.axonframework.eventsourcing.annotation.reflection.EntityCreator;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.MessageStream;
import org.axonframework.messaging.unitofwork.ProcessingContext;
import org.axonframework.modelling.annotation.InjectEntity;

import java.util.HashMap;
import java.util.Map;

public class WhenAllCoursesFullyBookedThenSendNotification {

    @EventSourcedEntity(tagKey = FacultyTags.COURSE_ID)
    record State(Map<CourseId, Course> courses, boolean notified) {

        record Course(int capacity, int students) {

            Course capacity(int newCapacity) {
                return new Course(newCapacity, this.students);
            }

            Course studentSubscribed() {
                return new Course(this.capacity, this.students + 1);
            }

            Course studentUnsubscribed() {
                return new Course(this.capacity, this.students - 1);
            }

            boolean isFullyBooked() {
                return students >= capacity;
            }

        }

        @EntityCreator
        State() {
            this(new HashMap<>(), false);
        }

        @EventSourcingHandler
        State evolve(CourseCreated event) {
            courses.put(event.courseId(), new Course(event.capacity(), 0, false));
            return new State(courses, notified);
        }

        @EventSourcingHandler
        State evolve(CourseCapacityChanged event) {
            courses.computeIfPresent(event.courseId(), (id, course) -> course.capacity(event.capacity()));
            return new State(courses, notified);
        }

        @EventSourcingHandler
        State evolve(StudentSubscribedToCourse event) {
            courses.computeIfPresent(event.courseId(), (id, course) -> course.studentSubscribed());
            return new State(courses, notified);
        }

        @EventSourcingHandler
        State evolve(StudentUnsubscribedFromCourse event) {
            courses.computeIfPresent(event.courseId(), (id, course) -> course.studentUnsubscribed());
            return new State(courses, notified);
        }

        @EventSourcingHandler
        State evolve(AllCoursesFullyBookedNotificationSent event) {
            return new State(courses, true);
        }
    }

    static class AutomationCommandHandler {

        @CommandHandler
        public void decide(
                SendAllCoursesFullyBookedNotification command,
                @InjectEntity State state,
                ProcessingContext context
        ) {
            var canNotify = state != null && !state.notified();
            if (canNotify) {
                var notification = new NotificationService.Notification("admin", "All courses are fully booked now.");
                context.component(NotificationService.class).sendNotification(notification);
                var eventAppender = EventAppender.forContext(context);
                eventAppender.append(new AllCoursesFullyBookedNotificationSent());
            }
        }
    }

    static class AutomationEventHandler {

        @EventHandler
        public MessageStream.Empty<?> react(
                StudentSubscribedToCourse event,
                @InjectEntity State state,
                ProcessingContext context
        ) {
            return sendNotificationIfAllCoursesFullyBooked(state, context);
        }

        @EventHandler
        public MessageStream.Empty<?> react(
                CourseCapacityChanged event,
                @InjectEntity State state,
                ProcessingContext context
        ) {
            return sendNotificationIfAllCoursesFullyBooked(state, context);
        }

        private MessageStream.Empty<Message> sendNotificationIfAllCoursesFullyBooked(State state, ProcessingContext context) {
            var automationState = state != null ? state : new State();
            var allCoursesFullyBooked = automationState.courses.values().stream().allMatch(State.Course::isFullyBooked);
            var shouldNotify = allCoursesFullyBooked && !automationState.notified();
            if (shouldNotify) {
                var commandGateway = context.component(CommandGateway.class);
                commandGateway.send(new SendAllCoursesFullyBookedNotification(), context);
            }
            return MessageStream.empty();
        }
    }


}
