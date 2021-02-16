package mekanism.api.math;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quicktheories.QuickTheory;
import org.quicktheories.WithQuickTheories;
import org.quicktheories.core.Gen;
import org.quicktheories.dsl.TheoryBuilder2;
import org.quicktheories.impl.Constraint;

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
        return clampFromBigDecimal(new BigDecimal(a.toString()).divide(new BigDecimal(b.toString()), 4, RoundingMode.HALF_UP));
    }

    private static FloatingLong divideViaBigDecimalFloor(FloatingLong a, FloatingLong b) {
        return clampFromBigDecimal(new BigDecimal(a.toString()).divide(new BigDecimal(b.toString()), 4, RoundingMode.FLOOR));
    }

    /**
     * Generator for all possible floating longs
     */
    private Gen<FloatingLong> allFloatingLongs() {
        //Value constraint is any possible long
        Constraint valueConstraint = Constraint.between(Long.MIN_VALUE, Long.MAX_VALUE).withShrinkPoint(0);
        //Decimal constraint is any possible decimal
        Constraint decimalConstraint = Constraint.between(0, 9_999).withShrinkPoint(0);
        //Given random generator create floating long using the two constraints we defined above]
        return prng -> FloatingLong.createConst(prng.next(valueConstraint), (short) prng.next(decimalConstraint));
    }

    private TheoryBuilder2<FloatingLong, FloatingLong> floatingLongPairTheory() {
        return qt().forAll(allFloatingLongs(), allFloatingLongs());
    }

    @Override
    public QuickTheory qt() {
        //Force our example count to be higher than the default by 100x
        return WithQuickTheories.super.qt().withExamples(100_000);
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
        floatingLongPairTheory().check((a, b) -> a.add(b).equals(addViaBigDecimal(a, b)));
    }

    @Test
    @DisplayName("Test subtracting and clamping at zero for underflow")
    void testSubtraction() {
        floatingLongPairTheory().check((a, b) -> a.subtract(b).equals(subtractViaBigDecimal(a, b)));
    }

    @Test
    @DisplayName("Test multiplying and clamping at max value for overflow")
    void testMultiplying() {
        floatingLongPairTheory().check((a, b) -> a.multiply(b).equals(multiplyViaBigDecimal(a, b)));
    }

    @Test
    @DisplayName("Test dividing and clamping at max value for overflow")
    void testDivision() {
        floatingLongPairTheory().check((a, b) -> b.isZero() || a.divide(b).equals(divideViaBigDecimal(a, b)));
    }

    @Test
    @DisplayName("Test dividing to unsigned long")
    void testDivisionToUnsignedLong() {
        floatingLongPairTheory().check((a, b) -> b.isZero() || a.divideToUnsignedLong(b) == divideViaBigDecimalFloor(a, b).getValue());
    }

    @Test
    @DisplayName("Test dividing to long")
    void testDivisionToLong() {
        floatingLongPairTheory().check((a, b) -> b.isZero() || a.divideToLong(b) == divideViaBigDecimalFloor(a, b).getValue());
    }

    @Test
    @DisplayName("Test dividing by long")
    void testDivisionByLong() {
        qt().forAll(
                allFloatingLongs(),
                longs().all()
        ).check((a, b) -> b == 0 || a.divide(b).equals(divideViaBigDecimal(a, FloatingLong.create(b))));
    }
}