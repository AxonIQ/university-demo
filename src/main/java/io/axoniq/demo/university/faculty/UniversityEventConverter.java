package io.axoniq.demo.university.faculty;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
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
 * Uses JSON serialization with Jackson for robust and maintainable format.
 *
 * @author Cursor AI
 */
public class UniversityEventConverter implements Converter {

    private static final Logger logger = LoggerFactory.getLogger(UniversityEventConverter.class);
    
    private final ObjectMapper objectMapper;
    private final Set<Class<?>> supportedEventTypes;

    public UniversityEventConverter() {
        this.objectMapper = createObjectMapper();
        this.supportedEventTypes = Set.of(
            CourseCreated.class,
            CourseRenamed.class,
            CourseCapacityChanged.class,
            StudentEnrolledInFaculty.class,
            StudentSubscribedToCourse.class,
            StudentUnsubscribedFromCourse.class
        );
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        
        // Custom serializers for ID classes
        module.addSerializer(CourseId.class, new CourseIdSerializer());
        module.addSerializer(StudentId.class, new StudentIdSerializer());
        module.addDeserializer(CourseId.class, new CourseIdDeserializer());
        module.addDeserializer(StudentId.class, new StudentIdDeserializer());
        
        mapper.registerModule(module);
        return mapper;
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
                String json = objectMapper.writeValueAsString(input);
                
                if (byte[].class.isAssignableFrom(targetType)) {
                    return targetType.cast(json.getBytes(StandardCharsets.UTF_8));
                } else if (String.class.isAssignableFrom(targetType)) {
                    return targetType.cast(json);
                }
            }
            
            // Deserialize from byte[] or String to event
            if (supportedEventTypes.contains(targetType)) {
                String json;
                
                if (byte[].class.isAssignableFrom(sourceType)) {
                    json = new String((byte[]) input, StandardCharsets.UTF_8);
                } else if (String.class.isAssignableFrom(sourceType)) {
                    json = (String) input;
                } else {
                    throw new IllegalArgumentException("Unsupported source type: " + sourceType);
                }
                
                return objectMapper.readValue(json, targetType);
            }
            
        } catch (JsonProcessingException e) {
            logger.error("Failed to convert between {} and {}", sourceType, targetType, e);
            throw new RuntimeException("Conversion failed", e);
        }
        
        throw new IllegalArgumentException("Cannot convert from " + sourceType + " to " + targetType);
    }

    // Custom serializers and deserializers for ID classes
    private static class CourseIdSerializer extends com.fasterxml.jackson.databind.JsonSerializer<CourseId> {
        @Override
        public void serialize(CourseId value, com.fasterxml.jackson.core.JsonGenerator gen, 
                             com.fasterxml.jackson.databind.SerializerProvider serializers) throws java.io.IOException {
            gen.writeString(value.raw());
        }
    }
    
    private static class StudentIdSerializer extends com.fasterxml.jackson.databind.JsonSerializer<StudentId> {
        @Override
        public void serialize(StudentId value, com.fasterxml.jackson.core.JsonGenerator gen, 
                             com.fasterxml.jackson.databind.SerializerProvider serializers) throws java.io.IOException {
            gen.writeString(value.raw());
        }
    }
    
    private static class CourseIdDeserializer extends com.fasterxml.jackson.databind.JsonDeserializer<CourseId> {
        @Override
        public CourseId deserialize(com.fasterxml.jackson.core.JsonParser p, 
                                   com.fasterxml.jackson.databind.DeserializationContext ctxt) throws java.io.IOException {
            return CourseId.of(p.getValueAsString());
        }
    }
    
    private static class StudentIdDeserializer extends com.fasterxml.jackson.databind.JsonDeserializer<StudentId> {
        @Override
        public StudentId deserialize(com.fasterxml.jackson.core.JsonParser p, 
                                    com.fasterxml.jackson.databind.DeserializationContext ctxt) throws java.io.IOException {
            return StudentId.of(p.getValueAsString());
        }
    }
} 