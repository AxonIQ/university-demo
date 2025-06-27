package io.axoniq.demo.university.faculty.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JacksonConverterTest {

    private JacksonConverter converter;

    record TestEvent(String id, String name, int value) {}
    record AnotherEvent(String userId, String description) {}

    @BeforeEach
    void setUp() {
        converter = new JacksonConverter();
    }

    @Test
    void shouldConvertTestEventToStringAndBack() {
        // given
        TestEvent original = new TestEvent("ID123", "TestName", 42);

        // when
        String serialized = converter.convert(original, String.class);
        TestEvent deserialized = converter.convert(serialized, TestEvent.class);

        // then
        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void shouldConvertTestEventToByteArrayAndBack() {
        // given
        TestEvent original = new TestEvent("ID456", "OtherName", 99);

        // when
        byte[] serialized = converter.convert(original, byte[].class);
        TestEvent deserialized = converter.convert(serialized, TestEvent.class);

        // then
        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void shouldConvertAnotherEventToStringAndBack() {
        // given
        AnotherEvent original = new AnotherEvent("USR001", "Some description");

        // when
        String serialized = converter.convert(original, String.class);
        AnotherEvent deserialized = converter.convert(serialized, AnotherEvent.class);

        // then
        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void shouldReturnTrueForSupportedConversions() {
        assertThat(converter.canConvert(TestEvent.class, String.class)).isTrue();
        assertThat(converter.canConvert(TestEvent.class, byte[].class)).isTrue();
        assertThat(converter.canConvert(String.class, TestEvent.class)).isTrue();
        assertThat(converter.canConvert(byte[].class, TestEvent.class)).isTrue();
        assertThat(converter.canConvert(AnotherEvent.class, String.class)).isTrue();
        assertThat(converter.canConvert(AnotherEvent.class, byte[].class)).isTrue();
    }

    @Test
    void shouldReturnSameInstanceIfSourceAndTargetTypeAreEqual() {
        TestEvent testEvent = new TestEvent("ID789", "SameType", 123);
        TestEvent result = converter.convert(testEvent, TestEvent.class, TestEvent.class);
        assertThat(result).isSameAs(testEvent);

        AnotherEvent anotherEvent = new AnotherEvent("USR002", "No conversion");
        AnotherEvent anotherResult = converter.convert(anotherEvent, AnotherEvent.class, AnotherEvent.class);
        assertThat(anotherResult).isSameAs(anotherEvent);
    }
}
