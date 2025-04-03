package io.axoniq.demo.university.faculty;

import io.axoniq.demo.university.faculty.write.createcourse.CreateCourseConfiguration;
import io.axoniq.demo.university.faculty.write.renamecourse.RenameCourseConfiguration;
import io.axoniq.demo.university.faculty.write.subscribestudent.SubscribeStudentConfiguration;
import io.axoniq.demo.university.faculty.write.unsubscribestudent.UnsubscribeStudentConfiguration;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;

public class FacultyModuleConfiguration {

    public static EventSourcingConfigurer configure(EventSourcingConfigurer configurer) {
        configurer = CreateCourseConfiguration.configure(configurer);
        configurer = RenameCourseConfiguration.configure(configurer);
        configurer = SubscribeStudentConfiguration.configure(configurer);
        configurer = UnsubscribeStudentConfiguration.configure(configurer);
        return configurer;
    }
}
