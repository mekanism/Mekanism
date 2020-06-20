package mekanism.api.math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NumericOverflow")
@DisplayName("Test the implementation of FloatingLong")
class FloatingLongTest {

    @Test
    @DisplayName("Test value past max clamping to the max value")
    void testMaxClamping() {
        Assertions.assertEquals(FloatingLong.MAX_VALUE, FloatingLong.create(-1, Short.MAX_VALUE));
    }

    @Test
    @DisplayName("Test basic multiplication")
    void testBasicMultiply() {
        FloatingLong a = FloatingLong.create(27.1);
        FloatingLong b = FloatingLong.create(47.1);
        Assertions.assertEquals(FloatingLong.createConst(1_276.41), a.multiply(b));
    }

    @Test
    @DisplayName("Test that the intermediate values are sanitized and merged properly with high decimal values")
    void testSanitation() {
        FloatingLong a = FloatingLong.create(5, (short) 6_789);
        FloatingLong b = FloatingLong.create(9, (short) 8_765);
        Assertions.assertEquals(FloatingLong.createConst(56.0876), a.multiply(b));
    }

    @Test
    @DisplayName("Test basic addition with a decimal overflow")
    void testBasicAdd() {
        FloatingLong a = FloatingLong.create(509_876, (short) 5_555);
        FloatingLong b = FloatingLong.create(13_479, (short) 6_789);
        Assertions.assertEquals(FloatingLong.createConst(523_356.2344), a.add(b));
    }

    @Test
    @DisplayName("Test addition where the value portion should overflow to negative")
    void testUnsignedOverFlowAdd() {
        FloatingLong a = FloatingLong.create(Long.MAX_VALUE, (short) 1);
        FloatingLong b = FloatingLong.create(9, (short) 2);
        Assertions.assertEquals(FloatingLong.create(Long.MAX_VALUE + 9, (short) 3), a.add(b));
    }

    @Test
    @DisplayName("Test addition where the value portion overflows")
    void testOverFlowAdd() {
        FloatingLong a = FloatingLong.create(Long.MAX_VALUE, (short) 1);
        FloatingLong b = FloatingLong.create(Long.MAX_VALUE + 2, (short) 2);
        Assertions.assertEquals(FloatingLong.MAX_VALUE, a.add(b));
    }

    @Test
    @DisplayName("Test addition where the decimal overflow causes the long to overflow")
    void testDecimalOverflowAdd() {
        FloatingLong a = FloatingLong.create(Long.MAX_VALUE, (short) 9_185);
        FloatingLong b = FloatingLong.create(Long.MAX_VALUE + 1, (short) 3_091);
        Assertions.assertEquals(FloatingLong.MAX_VALUE, a.add(b));
    }

    @Test
    @DisplayName("Test basic division")
    void testBasicDivision() {
        FloatingLong a = FloatingLong.create(6, (short) 1_000);
        FloatingLong b = FloatingLong.create(3, (short) 1_000);
        Assertions.assertEquals(FloatingLong.create(1.9677), a.divide(b));
    }

    @Test
    @DisplayName("Test division with a very large numerator")
    void testDivisionLargeNumerator() {
        FloatingLong a = FloatingLong.create(Long.MAX_VALUE);
        FloatingLong b = FloatingLong.create((long) 7 * 7 * 73 * 127 * 92_737);
        Assertions.assertEquals(FloatingLong.create((long) 649_657 * 337), a.divide(b));
    }

    @Test
    @DisplayName("Test to string as two decimals")
    void testConvertingStringToDecimal() {
        Assertions.assertEquals("0.00", FloatingLong.create(0, (short) 1).toString(2));
    }
}