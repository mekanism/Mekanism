package mekanism.api.math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test the implementation of FloatingLong")
class FloatingLongTest {

    @Test
    @DisplayName("Test value past max clamping to the max value")
    void testMaxClamping() {
        Assertions.assertEquals(FloatingLong.MAX_VALUE, FloatingLong.create(Long.MAX_VALUE, Short.MAX_VALUE));
    }
}