package io.axoniq.demo.university.faculty;

import io.axoniq.demo.university.faculty.events.*;
import io.axoniq.demo.university.shared.ids.CourseId;
import io.axoniq.demo.university.shared.ids.StudentId;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.axonframework.serialization.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Custom Converter implementation for university faculty events.
 * Handles serialization/deserialization of events in the io.axoniq.demo.university.faculty.events package.
 * Uses a simple custom string format for serialization.
 *
 * @author Cursor AI
 */
public class UniversityEventConverter implements Converter {

    private static final Logger logger = LoggerFactory.getLogger(UniversityEventConverter.class);
    private static final String DELIMITER = "|";
    
    private final Set<Class<?>> supportedEventTypes;

    public UniversityEventConverter() {
        this.supportedEventTypes = Set.of(
            CourseCreated.class,
            CourseRenamed.class,
            CourseCapacityChanged.class,
            StudentEnrolledInFaculty.class,
            StudentSubscribedToCourse.class,
            StudentUnsubscribedFromCourse.class
        );
    }

    @Override
    public boolean canConvert(@Nonnull Class<?> sourceType, @Nonnull Class<?> targetType) {
        // Handle serialization: event -> byte[] or String
        if (supportedEventTypes.contains(sourceType) && 
            (byte[].class.isAssignableFrom(targetType) || String.class.isAssignableFrom(targetType))) {
            return true;
        }
        
        // Handle deserialization: byte[] or String -> event
        if ((byte[].class.isAssignableFrom(sourceType) || String.class.isAssignableFrom(sourceType)) &&
            supportedEventTypes.contains(targetType)) {
            return true;
        }
        
        return false;
    }

    @Override
    @Nullable
    public <S, T> T convert(@Nullable S input, @Nonnull Class<S> sourceType, @Nonnull Class<T> targetType) {
        if (input == null) {
            return null;
        }

        try {
            // Serialize event to byte[] or String
            if (supportedEventTypes.contains(sourceType)) {
                String serialized = serializeEvent(input);
                
                if (byte[].class.isAssignableFrom(targetType)) {
                    return targetType.cast(serialized.getBytes(StandardCharsets.UTF_8));
                } else if (String.class.isAssignableFrom(targetType)) {
                    return targetType.cast(serialized);
                }
            }
            
            // Deserialize from byte[] or String to event
            if (supportedEventTypes.contains(targetType)) {
                String serialized;
                
                if (byte[].class.isAssignableFrom(sourceType)) {
                    serialized = new String((byte[]) input, StandardCharsets.UTF_8);
                } else if (String.class.isAssignableFrom(sourceType)) {
                    serialized = (String) input;
                } else {
                    throw new IllegalArgumentException("Unsupported source type: " + sourceType);
                }
                
                return deserializeEvent(serialized, targetType);
            }
            
        } catch (Exception e) {
            logger.error("Failed to convert between {} and {}", sourceType, targetType, e);
            throw new RuntimeException("Conversion failed", e);
        }
        
        throw new IllegalArgumentException("Cannot convert from " + sourceType + " to " + targetType);
    }

    private String serializeEvent(Object event) {
        String className = event.getClass().getSimpleName();
        
        return switch (event) {
            case CourseCreated courseCreated -> 
                String.format("%s%s%s%s%s%s%d", className, DELIMITER, 
                    courseCreated.courseId().raw(), DELIMITER, courseCreated.name(), DELIMITER, courseCreated.capacity());
                    
            case CourseRenamed courseRenamed -> 
                String.format("%s%s%s%s%s", className, DELIMITER, 
                    courseRenamed.courseId().raw(), DELIMITER, courseRenamed.name());
                    
            case CourseCapacityChanged courseCapacityChanged -> 
                String.format("%s%s%s%s%d", className, DELIMITER, 
                    courseCapacityChanged.courseId().raw(), DELIMITER, courseCapacityChanged.capacity());
                    
            case StudentEnrolledInFaculty studentEnrolled -> 
                String.format("%s%s%s%s%s%s%s", className, DELIMITER, 
                    studentEnrolled.studentId().raw(), DELIMITER, studentEnrolled.firstName(), DELIMITER, studentEnrolled.lastName());
                    
            case StudentSubscribedToCourse studentSubscribed -> 
                String.format("%s%s%s%s%s", className, DELIMITER, 
                    studentSubscribed.studentId().raw(), DELIMITER, studentSubscribed.courseId().raw());
                    
            case StudentUnsubscribedFromCourse studentUnsubscribed -> 
                String.format("%s%s%s%s%s", className, DELIMITER, 
                    studentUnsubscribed.studentId().raw(), DELIMITER, studentUnsubscribed.courseId().raw());
                    
            default -> throw new IllegalArgumentException("Unsupported event type: " + event.getClass());
        };
    }

    @SuppressWarnings("unchecked")
    private <T> T deserializeEvent(String serialized, Class<T> targetType) {
        String[] parts = serialized.split("\\" + DELIMITER);
        String className = parts[0];
        
        return (T) switch (className) {
            case "CourseCreated" -> {
                if (parts.length != 4) throw new IllegalArgumentException("Invalid CourseCreated format");
                yield new CourseCreated(CourseId.of(parts[1]), parts[2], Integer.parseInt(parts[3]));
            }
            case "CourseRenamed" -> {
                if (parts.length != 3) throw new IllegalArgumentException("Invalid CourseRenamed format");
                yield new CourseRenamed(CourseId.of(parts[1]), parts[2]);
            }
            case "CourseCapacityChanged" -> {
                if (parts.length != 3) throw new IllegalArgumentException("Invalid CourseCapacityChanged format");
                yield new CourseCapacityChanged(CourseId.of(parts[1]), Integer.parseInt(parts[2]));
            }
            case "StudentEnrolledInFaculty" -> {
                if (parts.length != 4) throw new IllegalArgumentException("Invalid StudentEnrolledInFaculty format");
                yield new StudentEnrolledInFaculty(StudentId.of(parts[1]), parts[2], parts[3]);
            }
            case "StudentSubscribedToCourse" -> {
                if (parts.length != 3) throw new IllegalArgumentException("Invalid StudentSubscribedToCourse format");
                yield new StudentSubscribedToCourse(StudentId.of(parts[1]), CourseId.of(parts[2]));
            }
            case "StudentUnsubscribedFromCourse" -> {
                if (parts.length != 3) throw new IllegalArgumentException("Invalid StudentUnsubscribedFromCourse format");
                yield new StudentUnsubscribedFromCourse(StudentId.of(parts[1]), CourseId.of(parts[2]));
            }
            default -> throw new IllegalArgumentException("Unknown event type: " + className);
        };
    }
} 