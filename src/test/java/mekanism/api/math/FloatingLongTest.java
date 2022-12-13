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
    @DisplayName("Test division with a very large denominator")
    void testDivisionLargeDenominator() {
        FloatingLong a = FloatingLong.create(922_355_340_224_119L);
        FloatingLong b = FloatingLong.create(-1L);
        Assertions.assertEquals(FloatingLong.create(0L, (short) 1), a.divide(b));
    }

    @Test
    @DisplayName("Test division with a very large numerator & denominator")
    void testDivisionLargeNumDen() {
        FloatingLong a = FloatingLong.create(1_844_724_002_681_593_706L);
        FloatingLong b = FloatingLong.create(-1L);
        Assertions.assertEquals(FloatingLong.create(0L, (short) 1_000), a.divide(b));
    }

    @Test
    @DisplayName("Test division bad case 1")
    void testDivisionBad1() {
        FloatingLong a = FloatingLong.create(1L);
        FloatingLong b = FloatingLong.create(32L);
        Assertions.assertEquals(FloatingLong.create(0L, (short) 313), a.divide(b));
    }

    @Test
    @DisplayName("Test division bad case 2")
    void testDivisionBad2() {
        FloatingLong a = FloatingLong.create(-1L);
        FloatingLong b = FloatingLong.create(184_948_298_500L);
        Assertions.assertEquals(99_740_004L, a.divideToUnsignedLong(b));
    }

    @Test
    @DisplayName("Test divisionToLong tough case")
    void testDivisionTough1() {
        FloatingLong a = FloatingLong.create(-705_739_103_007_515L - 1);
        FloatingLong b = FloatingLong.create(185_704_605_303L);
        Assertions.assertEquals(99_329_999L, a.divideToUnsignedLong(b));
    }

    @Test
    @DisplayName("Test divisionToUnsignedLong rounding case")
    void testDivisionUnsignedRound() {
        FloatingLong a = FloatingLong.create(-893_067_536_972_106_880L);
        FloatingLong b = FloatingLong.create(-892_518_212_563_104_587L, (short) 1);
        Assertions.assertEquals(0L, a.divideToUnsignedLong(b));
    }

    @Test
    @DisplayName("Test division by long, rounding case")
    void testDivisionRound() {
        FloatingLong a = FloatingLong.create(-3_361_844_618_367_640_110L);
        FloatingLong b = FloatingLong.create(3_016_988_510_907_153_472L);
        Assertions.assertEquals(FloatingLong.create(5L), a.divide(b));
    }


    @Test
    @DisplayName("Test dividing to long and clamping to the max long because of bounds.")
    void testDivideToLongIdentityClamp() {
        FloatingLong a = FloatingLong.create(-1);
        Assertions.assertEquals(a.divideToLong(FloatingLong.ONE), a.divide(FloatingLong.ONE).longValue());
    }

    @Test
    @DisplayName("Test division denominator underflow to 0")
    void testDivisionSmallDenominator() {
        FloatingLong a = FloatingLong.create(0, (short) 1);
        long b = 2L;
        Assertions.assertEquals(FloatingLong.create(0L, (short) 1), a.divide(b));
    }

    @Test
    @DisplayName("Test to string as two decimals")
    void testConvertingStringToDecimal() {
        Assertions.assertEquals("0.00", FloatingLong.create(0, (short) 1).toString(2));
    }
}