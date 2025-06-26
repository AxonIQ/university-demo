package io.axoniq.demo.university.faculty.infrastructure;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.axoniq.demo.university.shared.ids.CourseId;
import io.axoniq.demo.university.shared.ids.StudentId;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.axonframework.serialization.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Custom Converter implementation for university faculty events.
 * Handles serialization/deserialization of events in the io.axoniq.demo.university.faculty.events package.
 * Uses JSON serialization with Jackson for robust and maintainable format.
 *
 * @author GitHub Copilot
 */
public final class JacksonFacultyEventConverter implements Converter {

    private static final Logger logger = LoggerFactory.getLogger(JacksonFacultyEventConverter.class);
    
    private final ObjectMapper objectMapper;

    public JacksonFacultyEventConverter() {
        this.objectMapper = createObjectMapper();
    }

    private static ObjectMapper createObjectMapper() {
        var mapper = new ObjectMapper();
        var module = new SimpleModule();

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
        return sourceType.equals(targetType) || canSerialize(targetType) || canDeserialize(sourceType);
    }

    private static boolean canSerialize(Class<?> targetType) {
        return byte[].class.isAssignableFrom(targetType) || String.class.isAssignableFrom(targetType);
    }

    private static boolean canDeserialize(Class<?> sourceType) {
        return byte[].class.isAssignableFrom(sourceType) || String.class.isAssignableFrom(sourceType);
    }

    @Override
    @Nullable
    public <S, T> T convert(@Nullable S input, @Nonnull Class<S> sourceType, @Nonnull Class<T> targetType) {
        if (input == null) {
            return null;
        }

        if (sourceType.equals(targetType)) {
            return targetType.cast(input);
        }

        try {
            return performConversion(input, sourceType, targetType);
        } catch (JsonProcessingException e) {
            var errorMessage = """
                Failed to convert between %s and %s: %s
                """.formatted(sourceType.getSimpleName(), targetType.getSimpleName(), e.getMessage());
            logger.error(errorMessage, e);
            throw new ConversionException(errorMessage, e);
        }
    }

    @SuppressWarnings("unchecked")
    private <S, T> T performConversion(S input, Class<S> sourceType, Class<T> targetType)
            throws JsonProcessingException {

        // Handle serialization: event -> byte[] or String
        if (canSerialize(targetType)) {
            var json = objectMapper.writeValueAsString(input);
            return switch (targetType.getName()) {
                case "[B" -> (T) json.getBytes(StandardCharsets.UTF_8); // byte[]
                case "java.lang.String" -> (T) json;
                default -> throw new IllegalArgumentException("Unsupported target type: " + targetType);
            };
        }

        // Handle deserialization: byte[] or String -> event
        var jsonInput = switch (input) {
            case byte[] bytes -> new String(bytes, StandardCharsets.UTF_8);
            case String str -> str;
            default -> throw new IllegalArgumentException("Unsupported source type: " + sourceType);
        };

        return objectMapper.readValue(jsonInput, targetType);
    }

    // Custom serializers and deserializers for ID classes using modern sealed approach
    private static final class CourseIdSerializer extends JsonSerializer<CourseId> {
        @Override
        public void serialize(CourseId value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeString(value.raw());
        }
    }
    
    private static final class StudentIdSerializer extends JsonSerializer<StudentId> {
        @Override
        public void serialize(StudentId value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeString(value.raw());
        }
    }
    
    private static final class CourseIdDeserializer extends JsonDeserializer<CourseId> {
        @Override
        public CourseId deserialize(JsonParser parser, DeserializationContext context)
                throws IOException {
            return CourseId.of(parser.getValueAsString());
        }
    }
    
    private static final class StudentIdDeserializer extends JsonDeserializer<StudentId> {
        @Override
        public StudentId deserialize(JsonParser parser, DeserializationContext context)
                throws IOException {
            return StudentId.of(parser.getValueAsString());
        }
    }

    /**
     * Custom exception for conversion failures.
     */
    public static final class ConversionException extends RuntimeException {
        public ConversionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
