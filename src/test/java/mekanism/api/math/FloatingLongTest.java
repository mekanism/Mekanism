package mekanism.api.math;

import mekanism.api.math.FloatingLong;
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

    @Test
    @DisplayName("Test basic multiplication")
    void testBasicMultiply() {
        FloatingLong fl1 = FloatingLong.create(27.1);
        FloatingLong fl2 = FloatingLong.create(47.1);
        FloatingLong result = fl1.multiply(fl2);

        Assertions.assertEquals(FloatingLong.create((double)1276.41), result);
    }

    @Test
    @DisplayName("Test that the intermediate values are sanitized and merged properly with high decimal values")
    void testSanitation() {
        FloatingLong fl1 = FloatingLong.create((long)5, (short)6789);
        FloatingLong fl2 = FloatingLong.create((long)9, (short)8765);
        FloatingLong result = fl1.multiply(fl2);

        Assertions.assertEquals(FloatingLong.create((double)56.0876), result);
    }
}