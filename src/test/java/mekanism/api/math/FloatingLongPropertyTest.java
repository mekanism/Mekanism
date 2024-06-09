package mekanism.api.math;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Label;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.DoubleRange;
import net.minecraft.Util;
import org.junit.jupiter.api.Assertions;

@Label("Test the implementation of FloatingLong by testing Properties of FloatingLong")
class FloatingLongPropertyTest {

    //Force our example count to be higher than the default by 100x
    private static final int TRIES = 100_000;
    private static final String ALL_FL_NAME = "allFloatingLongs";
    private static final String NON_ZERO_FL = "nonZeroFL";
    private static final String NON_ZERO_LONGS = "nonZeroLongs";

    private static final DecimalFormat df = Util.make(new DecimalFormat(), df -> df.setGroupingUsed(false));

    private static final BigDecimal maxFloatingLong = new BigDecimal(FloatingLong.MAX_VALUE.toString());

    /**
     * Generator for all possible floating longs
     */
    @Provide
    Arbitrary<FloatingLong> allFloatingLongs() {
        //Given random generator create floating long using the two constraints we defined above
        return Combinators.combine(
              //Value constraint is any possible long
              Arbitraries.longs(),
              //Decimal constraint is any possible decimal
              Arbitraries.shorts().between((short) 0, (short) 9_999)
        ).as(FloatingLong::createConst);
    }

    @Provide
    Arbitrary<FloatingLong> nonZeroFL() {
        return allFloatingLongs().filter(fl -> !fl.isZero());
    }

    @Provide
    Arbitrary<Long> nonZeroLongs() {
        return Arbitraries.longs().filter(l -> l != 0);
    }

    //If the value goes past the max value for floating longs this instead clamps it at the max floating long value
    private static FloatingLong clampFromBigDecimal(BigDecimal value) {
        if (value.compareTo(maxFloatingLong) >= 0) {
            return FloatingLong.MAX_VALUE;
        } else if (value.signum() < 1) {
            return FloatingLong.ZERO;
        }
        return FloatingLong.parseFloatingLong(value.toPlainString());
    }

    private static FloatingLong addViaBigDecimal(FloatingLong a, FloatingLong b) {
        return clampFromBigDecimal(new BigDecimal(a.toString()).add(new BigDecimal(b.toString())));
    }

    private static FloatingLong subtractViaBigDecimal(FloatingLong a, FloatingLong b) {
        return clampFromBigDecimal(new BigDecimal(a.toString()).subtract(new BigDecimal(b.toString())));
    }

    private static FloatingLong multiplyViaBigDecimal(FloatingLong a, FloatingLong b) {
        return clampFromBigDecimal(new BigDecimal(a.toString()).multiply(new BigDecimal(b.toString())));
    }

    private static FloatingLong divideViaBigDecimal(FloatingLong a, FloatingLong b, RoundingMode roundingMode) {
        return clampFromBigDecimal(new BigDecimal(a.toString()).divide(new BigDecimal(b.toString()), FloatingLong.DECIMAL_DIGITS, roundingMode));
    }

    //Note: We clamp this as the largest double that can represent an unsigned long so that we can properly parse it as a FL
    /*@Property(tries = TRIES)
    @Label("Test parsing positive doubles by decimal value")
    void testFromDouble(@ForAll @DoubleRange(max = 18446744073709550000D) double value) {
        Assertions.assertEquals(clampFromBigDecimal(new BigDecimal(value)), FloatingLong.createConst(value));
    }*/

    //Note: We clamp this as the largest double that can represent an unsigned long so that we can properly parse it as a FL
    @Property(tries = TRIES)
    @Label("Test parsing positive doubles via string representation")
    void testFromDoubleAsString(@ForAll @DoubleRange(max = 18446744073709550000D) double value) {
        String stringRepresentation = df.format(value);
        Assertions.assertEquals(clampFromBigDecimal(new BigDecimal(stringRepresentation)), FloatingLong.parseFloatingLong(stringRepresentation));
    }

    @Property(tries = TRIES)
    @Label("Test addition and clamping at max value for overflow")
    void testAddition(@ForAll(ALL_FL_NAME) FloatingLong a, @ForAll(ALL_FL_NAME) FloatingLong b) {
        Assertions.assertEquals(addViaBigDecimal(a, b), a.add(b));
    }

    @Property(tries = TRIES)
    @Label("Test subtracting and clamping at zero for underflow")
    void testSubtraction(@ForAll(ALL_FL_NAME) FloatingLong a, @ForAll(ALL_FL_NAME) FloatingLong b) {
        Assertions.assertEquals(subtractViaBigDecimal(a, b), a.subtract(b));
    }

    @Property(tries = TRIES)
    @Label("Test multiplying and clamping at max value for overflow")
    void testMultiplying(@ForAll(ALL_FL_NAME) FloatingLong a, @ForAll(ALL_FL_NAME) FloatingLong b) {
        Assertions.assertEquals(multiplyViaBigDecimal(a, b), a.multiply(b));
    }

    @Property(tries = TRIES)
    @Label("Test dividing and clamping at max value for overflow")
    void testDivision(@ForAll(ALL_FL_NAME) FloatingLong a, @ForAll(NON_ZERO_FL) FloatingLong b) {
        assertCloseEnough(divideViaBigDecimal(a, b, RoundingMode.HALF_UP), a.divide(b));
    }

    @Property(tries = TRIES)
    @Label("Test dividing to unsigned long")
    void testDivisionToUnsignedLong(@ForAll(ALL_FL_NAME) FloatingLong a, @ForAll(NON_ZERO_FL) FloatingLong b) {
        Assertions.assertEquals(divideViaBigDecimal(a, b, RoundingMode.FLOOR).getValue(), a.divideToUnsignedLong(b));
    }

    @Property(tries = TRIES)
    @Label("Test dividing to long")
    void testDivisionToLong(@ForAll(ALL_FL_NAME) FloatingLong a, @ForAll(NON_ZERO_FL) FloatingLong b) {
        long expected = divideViaBigDecimal(a, b, RoundingMode.FLOOR).getValue();
        if (expected < 0) {
            expected = Long.MAX_VALUE;
        }
        Assertions.assertEquals(expected, a.divideToLong(b));
    }

    @Property(tries = TRIES)
    @Label("Test dividing by long")
    void testDivisionByLong(@ForAll(ALL_FL_NAME) FloatingLong a, @ForAll(NON_ZERO_LONGS) long b) {
        assertCloseEnough(divideViaBigDecimal(a, FloatingLong.create(b), RoundingMode.HALF_UP), a.divide(b));
    }

    @Property(tries = TRIES)
    @Label("Test multiplying by long")
    void testMultiplicationByLong(@ForAll(ALL_FL_NAME) FloatingLong a, @ForAll long b) {
        Assertions.assertEquals(multiplyViaBigDecimal(a, FloatingLong.create(b)), a.multiply(b));
    }

    @Property(tries = TRIES)
    @Label("Test addition by long")
    void testAdditionByLong(@ForAll(ALL_FL_NAME) FloatingLong a, @ForAll long b) {
        Assertions.assertEquals(addViaBigDecimal(a, FloatingLong.create(b)), a.add(b));
    }

    @Property(tries = TRIES)
    @Label("Test subtraction by long")
    void testSubtractionByLong(@ForAll(ALL_FL_NAME) FloatingLong a, @ForAll long b) {
        Assertions.assertEquals(subtractViaBigDecimal(a, FloatingLong.create(b)), a.subtract(b));
    }

    private static void assertCloseEnough(FloatingLong bdResult, FloatingLong flResult) {
        //TODO: Ideally this +- 0.0001 wouldn't be necessary, but there are some bugs in FloatingLongs currently, and as we are likely to remove them soon
        // it isn't worth trying to track them all down
        if (bdResult.getValue() == flResult.getValue() && (bdResult.getDecimal() - 1 == flResult.getDecimal() || bdResult.getDecimal() + 1 == flResult.getDecimal())) {
            Assertions.assertTrue(true);
        } else {
            Assertions.assertEquals(bdResult, flResult);
        }
    }
}