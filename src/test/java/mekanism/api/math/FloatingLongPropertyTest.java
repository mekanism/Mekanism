package mekanism.api.math;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quicktheories.WithQuickTheories;
import org.quicktheories.dsl.TheoryBuilder4;

@DisplayName("Test the implementation of FloatingLong by testing Properties of FloatingLong")
class FloatingLongPropertyTest implements WithQuickTheories {

    private static final BigDecimal maxFloatingLong = new BigDecimal(FloatingLong.MAX_VALUE.toString());

    //If the value goes past the max value for floating longs this instead clamps it at the max floating long value
    private static FloatingLong clampFromBigDecimal(BigDecimal value) {
        if (value.compareTo(maxFloatingLong) >= 0) {
            return FloatingLong.MAX_VALUE;
        } else if (value.compareTo(BigDecimal.ZERO) <= 0) {
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

    private static FloatingLong divideViaBigDecimal(FloatingLong a, FloatingLong b) {
        return clampFromBigDecimal(new BigDecimal(a.toString()).divide(new BigDecimal(b.toString()), 4, RoundingMode.HALF_EVEN));
    }

    private TheoryBuilder4<Long, Integer, Long, Integer> theoryForAllPairs() {
        return qt().forAll(
              longs().all(),
              integers().between(0, 9_999),
              longs().all(),
              integers().between(0, 9_999)
        );
    }

    @Test
    @DisplayName("Test parsing positive doubles")
    void testFromDouble() {
        qt().forAll(doubles().between(0, Double.MAX_VALUE))
              .check(value -> FloatingLong.createConst(value).equals(clampFromBigDecimal(new BigDecimal(Double.toString(value)))));
    }

    @Test
    @DisplayName("Test addition and clamping at max value for overflow")
    void testAddition() {
        theoryForAllPairs().check((v1, d1, v2, d2) -> {
            FloatingLong a = FloatingLong.createConst(v1, d1.shortValue());
            FloatingLong b = FloatingLong.createConst(v2, d2.shortValue());
            return a.add(b).equals(addViaBigDecimal(a, b));
        });
    }

    @Test
    @DisplayName("Test subtracting and clamping at zero for underflow")
    void testSubtraction() {
        theoryForAllPairs().check((v1, d1, v2, d2) -> {
            FloatingLong a = FloatingLong.createConst(v1, d1.shortValue());
            FloatingLong b = FloatingLong.createConst(v2, d2.shortValue());
            return a.subtract(b).equals(subtractViaBigDecimal(a, b));
        });
    }

    @Test
    @DisplayName("Test multiplying and clamping at max value for overflow")
    void testMultiplying() {
        theoryForAllPairs().check((v1, d1, v2, d2) -> {
            FloatingLong a = FloatingLong.createConst(v1, d1.shortValue());
            FloatingLong b = FloatingLong.createConst(v2, d2.shortValue());
            return a.multiply(b).equals(multiplyViaBigDecimal(a, b));
        });
    }

    @Test
    @DisplayName("Test dividing and clamping at max value for overflow")
    void testDivision() {
        theoryForAllPairs().check((v1, d1, v2, d2) -> {
            FloatingLong a = FloatingLong.createConst(v1, d1.shortValue());
            FloatingLong b = FloatingLong.createConst(v2, d2.shortValue());
            return b.isZero() || a.divide(b).equals(divideViaBigDecimal(a, b));
        });
    }
}