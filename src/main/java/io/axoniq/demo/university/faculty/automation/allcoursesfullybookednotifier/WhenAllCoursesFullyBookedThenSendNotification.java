package io.axoniq.demo.university.faculty.automation.allcoursesfullybookednotifier;

import io.axoniq.demo.university.faculty.FacultyTags;
import io.axoniq.demo.university.faculty.Ids;
import io.axoniq.demo.university.shared.application.notifier.NotificationService;
import io.axoniq.demo.university.faculty.events.*;
import io.axoniq.demo.university.shared.ids.CourseId;
import org.axonframework.messaging.commandhandling.annotation.CommandHandler;
import org.axonframework.messaging.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.eventhandling.annotation.EventHandler;
import org.axonframework.messaging.eventhandling.gateway.EventAppender;
import org.axonframework.eventsourcing.annotation.EventSourcingHandler;
import org.axonframework.eventsourcing.annotation.reflection.EntityCreator;
import org.axonframework.messaging.core.Message;
import org.axonframework.messaging.core.MessageStream;
import org.axonframework.messaging.core.unitofwork.ProcessingContext;
import org.axonframework.modelling.StateManager;
import org.axonframework.modelling.annotation.InjectEntity;
import org.springframework.stereotype.Component;
import org.axonframework.extension.spring.stereotype.EventSourced;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the automation of sending a Command some condition is met.
 * It's a Stateful Event Handler that reacts on multiple events and tracks the state (left spots) of courses.
 *
 * The implementation uses event soured entity to track the state of courses and their capacities.
 * When all courses are determined to be fully booked, a notification is sent to the appropriate recipient.
 *
 * The functionality includes:
 * - Managing the state of courses (availability and subscription levels).
 * - Reacting to events such as course capacity updates, student subscriptions, and unsubscriptions.
 * - Assessing whether all courses are fully booked and sending a notification if necessary.
 */
@Component
public class WhenAllCoursesFullyBookedThenSendNotification {

    private static final String FACULTY_ID = "ONLY_FACULTY_ID";

    @EventSourced(idType = CourseId.class)
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
            courses.put(event.courseId(), new Course(event.capacity(), 0));
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
                @InjectEntity(idProperty = FacultyTags.FACULTY_ID) State state,
                ProcessingContext context
        ) {
            var canNotify = state != null && !state.notified();
            if (canNotify) {
                var notification = new NotificationService.Notification("admin", "All courses are fully booked now.");
                context.component(NotificationService.class).sendNotification(notification);
                var eventAppender = EventAppender.forContext(context);
                eventAppender.append(new AllCoursesFullyBookedNotificationSent(command.facultyId()));
            }
        }
    }

    static class AutomationEventHandler {

        @EventHandler
        public MessageStream.Empty<?> react(
                StudentSubscribedToCourse event,
                ProcessingContext context
        ) {
            var state = context.component(StateManager.class).loadEntity(State.class, FACULTY_ID, context).join();
            return sendNotificationIfAllCoursesFullyBooked(state, context);
        }

        @EventHandler
        public MessageStream.Empty<?> react(
                CourseCapacityChanged event,
                ProcessingContext context
        ) {
            var state = context.component(StateManager.class).loadEntity(State.class, FACULTY_ID, context).join();
            return sendNotificationIfAllCoursesFullyBooked(state, context);
        }

        private MessageStream.Empty<Message> sendNotificationIfAllCoursesFullyBooked(State state, ProcessingContext context) {
            var automationState = state != null ? state : new State();
            var allCoursesFullyBooked = automationState.courses.values().stream().allMatch(State.Course::isFullyBooked);
            var shouldNotify = allCoursesFullyBooked && !automationState.notified();
            if (shouldNotify) {
                var commandGateway = context.component(CommandGateway.class);
                commandGateway.send(new SendAllCoursesFullyBookedNotification(Ids.FACULTY_ID), context);
            }
            return MessageStream.empty();
        }
    }


}
