package mekanism.api.math;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quicktheories.WithQuickTheories;
import org.quicktheories.dsl.TheoryBuilder4;

@DisplayName("Test the implementation of FloatingLong by testing Properties of FloatingLong")
class FloatingLongPropertyTest implements WithQuickTheories {

    //TODO: Add tests for division and subtraction

    private static final BigDecimal maxFloatingLong = new BigDecimal(FloatingLong.MAX_VALUE.toString());

    //If the value goes past the max value for floating longs this instead clamps it at the max floating long value
    private static FloatingLong clampFromBigDecimal(BigDecimal value) {
        return value.compareTo(maxFloatingLong) > 0 ? FloatingLong.MAX_VALUE : FloatingLong.parseFloatingLong(value.toString());
    }

    private static FloatingLong multiplyViaBigDecimal(FloatingLong a, FloatingLong b) {
        return clampFromBigDecimal(new BigDecimal(a.toString()).multiply(new BigDecimal(b.toString())));
    }

    private static FloatingLong addViaBigDecimal(FloatingLong a, FloatingLong b) {
        return clampFromBigDecimal(new BigDecimal(a.toString()).add(new BigDecimal(b.toString())));
    }

    private TheoryBuilder4<Long, Integer, Long, Integer> theoryForAllPairs() {
        return qt().forAll(longs().between(0, Long.MAX_VALUE),
              integers().between(0, 9_999),
              longs().between(0, Long.MAX_VALUE),
              integers().between(0, 9_999)
        );
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
    @DisplayName("Test addition and clamping at max value for overflow")
    void testAddition() {
        theoryForAllPairs().check((v1, d1, v2, d2) -> {
            FloatingLong a = FloatingLong.createConst(v1, d1.shortValue());
            FloatingLong b = FloatingLong.createConst(v2, d2.shortValue());
            return a.add(b).equals(addViaBigDecimal(a, b));
        });
    }
}