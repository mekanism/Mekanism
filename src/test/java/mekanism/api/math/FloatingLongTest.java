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

    void testBasicMultiply() {
        FloatingLong fl1 = FloatingLong.create((long)27, (short)1);
        FloatingLong fl2 = FloatingLong.create((long)47, (short)1);
        FloatingLong result = fl1.multiply(fl2);

        Assertions.assertEquals(result, FloatingLong.create(1276.41));
    }

    void testSanitation() {
        FloatingLong fl1 = FloatingLong.create((long)1000000, (short)789);
        FloatingLong fl2 = FloatingLong.create((long)1000000, (short)987);
        FloatingLong result = fl1.multiply(fl2);

        Assertions.assertEquals(result, FloatingLong.create(1.0000018e+12));
    }
}