package mekanism.common.integration.computer.computercraft;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.nbt.ByteArrayNBT;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.CollectionNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.LongArrayNBT;
import net.minecraft.nbt.LongNBT;
import net.minecraft.nbt.NumberNBT;
import net.minecraft.nbt.ShortNBT;
import net.minecraft.nbt.StringNBT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quicktheories.QuickTheory;
import org.quicktheories.WithQuickTheories;
import org.quicktheories.core.Gen;
import org.quicktheories.impl.Constraint;

@DisplayName("Test ComputerCraft Argument Wrapper using properties")
class CCArgumentWrapperPropertyTest implements WithQuickTheories {

    @Override
    public QuickTheory qt() {
        //Force our example count to be higher than the default by 10x
        return WithQuickTheories.super.qt().withExamples(10_000);
    }

    private Gen<Byte> allBytes() {
        Constraint byteConstraint = Constraint.between(Byte.MIN_VALUE, Byte.MAX_VALUE).withShrinkPoint(0);
        return prng -> (byte) prng.next(byteConstraint);
    }

    private Gen<Short> allShorts() {
        Constraint shortConstraint = Constraint.between(Short.MIN_VALUE, Short.MAX_VALUE).withShrinkPoint(0);
        return prng -> (short) prng.next(shortConstraint);
    }

    private Gen<Short> onlyShorts() {
        Constraint negativeConstraint = Constraint.between(Short.MIN_VALUE, Byte.MIN_VALUE - 1).withShrinkPoint(0);
        Constraint positiveConstraint = Constraint.between(Byte.MAX_VALUE + 1, Short.MAX_VALUE).withShrinkPoint(0);
        Gen<Short> negativeGen = prng -> (short) prng.next(negativeConstraint);
        Gen<Short> positiveGen = prng -> (short) prng.next(positiveConstraint);
        return negativeGen.mix(positiveGen);
    }

    private Gen<Integer> onlyInts() {
        return integers().between(Integer.MIN_VALUE, Short.MIN_VALUE - 1).mix(
              integers().between(Short.MAX_VALUE + 1, Integer.MAX_VALUE));
    }

    private Gen<Long> onlyLongs() {
        return longs().between(Long.MIN_VALUE, Integer.MIN_VALUE - 1L).mix(
              longs().between(Integer.MAX_VALUE + 1L, Long.MAX_VALUE));
    }

    private Gen<Float> anyFloat() {
        return floats().any().assuming(f -> f != Float.NEGATIVE_INFINITY && f != Float.POSITIVE_INFINITY);
    }

    private Gen<Double> anyDouble() {
        return doubles().any().assuming(d -> d != Double.NEGATIVE_INFINITY && d != Double.POSITIVE_INFINITY);
    }

    // ===================
    // Bytes
    // ===================
    @Test
    @DisplayName("Test serializing and deserializing bytes with ByteNBT target and no hints")
    void testBytes() {
        qt().forAll(allBytes()).as(ByteNBT::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing bytes with INBT target and no hints")
    void testBytesINBT() {
        qt().forAll(allBytes()).as(ByteNBT::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, INBT.class, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing bytes with NumberNBT target and no hints")
    void testBytesNumberNBT() {
        qt().forAll(allBytes()).as(ByteNBT::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, NumberNBT.class, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing bytes with ByteNBT target and type hints")
    void testBytesWithHint() {
        qt().forAll(allBytes()).as(ByteNBT::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing bytes with INBT target and type hints")
    void testBytesINBTWithHint() {
        qt().forAll(allBytes()).as(ByteNBT::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, INBT.class, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing bytes with NumberNBT target and type hints")
    void testBytesNumberNBTWithHint() {
        qt().forAll(allBytes()).as(ByteNBT::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, NumberNBT.class, true));
    }

    // ===================
    // Shorts
    // ===================
    private boolean checkSameShort(short value, @Nullable Class<? extends INBT> targetClass) {
        ShortNBT nbt = ShortNBT.valueOf(value);
        Object sanitized = CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, targetClass, false);
        if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
            return nbt.equals(sanitized);
        }
        return ByteNBT.valueOf((byte) value).equals(sanitized);
    }

    @Test
    @DisplayName("Test serializing and deserializing shorts with ShortNBT target and no hints")
    void testShorts() {
        qt().forAll(allShorts()).as(ShortNBT::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing shorts with INBT target and no hints")
    void testShortsINBT() {
        qt().forAll(allShorts()).check(value -> checkSameShort(value, INBT.class));
    }

    @Test
    @DisplayName("Test serializing and deserializing shorts with NumberNBT target and no hints")
    void testShortsNumberNBT() {
        qt().forAll(allShorts()).check(value -> checkSameShort(value, NumberNBT.class));
    }

    @Test
    @DisplayName("Test serializing and deserializing shorts with ShortNBT target and type hints")
    void testShortsWithHint() {
        qt().forAll(allShorts()).as(ShortNBT::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing shorts with INBT target and type hints")
    void testShortsINBTWithHint() {
        qt().forAll(allShorts()).as(ShortNBT::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, INBT.class, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing shorts with NumberNBT target and type hints")
    void testShortsNumberNBTWithHint() {
        qt().forAll(allShorts()).as(ShortNBT::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, NumberNBT.class, true));
    }

    // ===================
    // Ints
    // ===================
    private boolean checkSameInt(int value, @Nullable Class<? extends INBT> targetClass) {
        IntNBT nbt = IntNBT.valueOf(value);
        Object sanitized = CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, targetClass, false);
        if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
            return nbt.equals(sanitized);
        } else if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
            return ShortNBT.valueOf((short) value).equals(sanitized);
        }
        return ByteNBT.valueOf((byte) value).equals(sanitized);
    }

    @Test
    @DisplayName("Test serializing and deserializing ints with IntNBT target and no hints")
    void testInts() {
        qt().forAll(integers().all()).as(IntNBT::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing ints with INBT target and no hints")
    void testIntsINBT() {
        qt().forAll(integers().all()).check(value -> checkSameInt(value, INBT.class));
    }

    @Test
    @DisplayName("Test serializing and deserializing ints with NumberNBT target and no hints")
    void testIntsNumberNBT() {
        qt().forAll(integers().all()).check(value -> checkSameInt(value, NumberNBT.class));
    }

    @Test
    @DisplayName("Test serializing and deserializing ints with IntNBT target and type hints")
    void testIntsWithHint() {
        qt().forAll(integers().all()).as(IntNBT::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing ints with INBT target and type hints")
    void testIntsINBTWithHint() {
        qt().forAll(integers().all()).as(IntNBT::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, INBT.class, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing ints with NumberNBT target and type hints")
    void testIntsNumberNBTWithHint() {
        qt().forAll(integers().all()).as(IntNBT::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, NumberNBT.class, true));
    }

    // ===================
    // Longs
    // ===================
    private LongNBT getExpected(long l) {
        //Adjust for floating point accuracy as needed
        if (l != (long) (double) l) {
            return LongNBT.valueOf((long) (double) l);
        }
        return LongNBT.valueOf(l);
    }

    private boolean checkSameLong(long value, @Nullable Class<? extends INBT> targetClass) {
        LongNBT nbt = LongNBT.valueOf(value);
        Object sanitized = CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, targetClass, false);
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            return getExpected(value).equals(sanitized);
        } else if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
            return IntNBT.valueOf((int) value).equals(sanitized);
        } else if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
            return ShortNBT.valueOf((short) value).equals(sanitized);
        }
        return ByteNBT.valueOf((byte) value).equals(sanitized);
    }

    private boolean checkSameLong(long value, @Nullable Class<? extends INBT> targetClass, boolean includeHints) {
        LongNBT nbt = LongNBT.valueOf(value);
        Object sanitized = CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, targetClass, includeHints);
        return getExpected(value).equals(sanitized);
    }

    @Test
    @DisplayName("Test serializing and deserializing longs with LongNBT target and no hints")
    void testLongs() {
        qt().forAll(longs().all()).check(value -> checkSameLong(value, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing longs with INBT target and no hints")
    void testLongsINBT() {
        qt().forAll(longs().all()).check(value -> checkSameLong(value, INBT.class));
    }

    @Test
    @DisplayName("Test serializing and deserializing longs with NumberNBT target and no hints")
    void testLongsNumberNBT() {
        qt().forAll(longs().all()).check(value -> checkSameLong(value, NumberNBT.class));
    }

    @Test
    @DisplayName("Test serializing and deserializing longs with LongNBT target and type hints")
    void testLongsWithHint() {
        qt().forAll(longs().all()).check(value -> checkSameLong(value, null, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing longs with INBT target and type hints")
    void testLongsINBTWithHint() {
        qt().forAll(longs().all()).check(value -> checkSameLong(value, INBT.class, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing longs with NumberNBT target and type hints")
    void testLongsNumberNBTWithHint() {
        qt().forAll(longs().all()).check(value -> checkSameLong(value, NumberNBT.class, true));
    }

    // ===================
    // Floats
    // ===================
    private boolean checkSameFloat(float value, @Nullable Class<? extends INBT> targetClass) {
        FloatNBT nbt = FloatNBT.valueOf(value);
        Object sanitized = CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, targetClass, false);
        if (value == Math.floor(value) && value >= Long.MIN_VALUE && value <= Long.MAX_VALUE) {
            //If we are an integer float check the proper type
            if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
                return LongNBT.valueOf((long) value).equals(sanitized);
            } else if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
                return IntNBT.valueOf((int) value).equals(sanitized);
            } else if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
                return ShortNBT.valueOf((short) value).equals(sanitized);
            }
            return ByteNBT.valueOf((byte) value).equals(sanitized);
        }
        return nbt.equals(sanitized);
    }

    @Test
    @DisplayName("Test serializing and deserializing floats with FloatNBT target and no hints")
    void testFloats() {
        qt().forAll(anyFloat()).as(FloatNBT::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing floats with INBT target and no hints")
    void testFloatsINBT() {
        qt().forAll(anyFloat()).check(value -> checkSameFloat(value, INBT.class));
    }

    @Test
    @DisplayName("Test serializing and deserializing floats with NumberNBT target and no hints")
    void testFloatsNumberNBT() {
        qt().forAll(anyFloat()).check(value -> checkSameFloat(value, NumberNBT.class));
    }

    @Test
    @DisplayName("Test serializing and deserializing floats with FloatNBT target and type hints")
    void testFloatsWithHint() {
        qt().forAll(anyFloat()).as(FloatNBT::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing floats with INBT target and type hints")
    void testFloatsINBTWithHint() {
        qt().forAll(anyFloat()).as(FloatNBT::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, INBT.class, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing floats with NumberNBT target and type hints")
    void testFloatsNumberNBTWithHint() {
        qt().forAll(anyFloat()).as(FloatNBT::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, NumberNBT.class, true));
    }

    // ===================
    // Doubles
    // ===================
    private boolean checkSameDouble(double value, @Nullable Class<? extends INBT> targetClass) {
        DoubleNBT nbt = DoubleNBT.valueOf(value);
        Object sanitized = CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, targetClass, false);
        if (value == Math.floor(value) && value >= Long.MIN_VALUE && value <= Long.MAX_VALUE) {
            //If we are an integer double-check the proper type
            if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
                return LongNBT.valueOf((long) value).equals(sanitized);
            } else if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
                return IntNBT.valueOf((int) value).equals(sanitized);
            } else if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
                return ShortNBT.valueOf((short) value).equals(sanitized);
            }
            return ByteNBT.valueOf((byte) value).equals(sanitized);
        }
        if (value < -Float.MAX_VALUE || value > Float.MAX_VALUE) {
            return nbt.equals(sanitized);
        }
        return FloatNBT.valueOf((float) value).equals(sanitized);
    }

    @Test
    @DisplayName("Test serializing and deserializing doubles with DoubleNBT target and no hints")
    void testDoubles() {
        qt().forAll(anyDouble()).as(DoubleNBT::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing doubles with INBT target and no hints")
    void testDoublesINBT() {
        qt().forAll(anyDouble()).check(value -> checkSameDouble(value, INBT.class));
    }

    @Test
    @DisplayName("Test serializing and deserializing doubles with NumberNBT target and no hints")
    void testDoublesNumberNBT() {
        qt().forAll(anyDouble()).check(value -> checkSameDouble(value, NumberNBT.class));
    }

    @Test
    @DisplayName("Test serializing and deserializing doubles with DoubleNBT target and type hints")
    void testDoublesWithHint() {
        qt().forAll(anyDouble()).as(DoubleNBT::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing doubles with INBT target and type hints")
    void testDoublesINBTWithHint() {
        qt().forAll(anyDouble()).as(DoubleNBT::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, INBT.class, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing doubles with NumberNBT target and type hints")
    void testDoublesNumberNBTWithHint() {
        qt().forAll(anyDouble()).as(DoubleNBT::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, NumberNBT.class, true));
    }

    // ===================
    // Strings
    // ===================
    @Test
    @DisplayName("Test serializing and deserializing strings with StringNBT target and no hints")
    void testStrings() {
        qt().forAll(strings().ascii().ofLengthBetween(0, 15)).as(StringNBT::valueOf)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing strings with INBT target and no hints")
    void testStringsINBT() {
        qt().forAll(strings().ascii().ofLengthBetween(0, 15)).as(StringNBT::valueOf)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, INBT.class, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing strings with StringNBT target and type hints")
    void testStringsWithHint() {
        qt().forAll(strings().ascii().ofLengthBetween(0, 15)).as(StringNBT::valueOf)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing strings with INBT target and type hints")
    void testStringsINBTWithHint() {
        qt().forAll(strings().ascii().ofLengthBetween(0, 15)).as(StringNBT::valueOf)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, INBT.class, true));
    }

    // ===================
    // Byte Arrays
    // ===================
    private boolean checkSameByteArray(ByteArrayNBT nbt, @Nullable Class<? extends INBT> targetClass) {
        Object sanitized = CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, targetClass, false);
        if (nbt.isEmpty()) {
            if (targetClass == INBT.class) {
                return sanitized instanceof CompoundNBT && ((CompoundNBT) sanitized).isEmpty();
            }//CollectionNBT
            return sanitized instanceof ListNBT && ((ListNBT) sanitized).isEmpty();
        }
        return nbt.equals(sanitized);
    }

    @Test
    @DisplayName("Test serializing and deserializing byte arrays with ByteArrayNBT target and no hints")
    void testByteArrays() {
        qt().forAll(lists().of(allBytes()).ofSizeBetween(0, 15)).as(ByteArrayNBT::new)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing byte arrays with INBT target and no hints")
    void testByteArraysINBT() {
        qt().forAll(lists().of(allBytes()).ofSizeBetween(0, 15)).as(ByteArrayNBT::new).check(nbt -> checkSameByteArray(nbt, INBT.class));
    }

    @Test
    @DisplayName("Test serializing and deserializing byte arrays with CollectionNBT target and no hints")
    void testByteArraysCollectionNBT() {
        qt().forAll(lists().of(allBytes()).ofSizeBetween(0, 15)).as(ByteArrayNBT::new).check(nbt -> checkSameByteArray(nbt, CollectionNBT.class));
    }

    @Test
    @DisplayName("Test serializing and deserializing byte arrays with ByteArrayNBT target and type hints")
    void testByteArraysWithHint() {
        qt().forAll(lists().of(allBytes()).ofSizeBetween(0, 15)).as(ByteArrayNBT::new)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing byte arrays with INBT target and type hints")
    void testByteArraysINBTWithHint() {
        qt().forAll(lists().of(allBytes()).ofSizeBetween(0, 15)).as(ByteArrayNBT::new)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, INBT.class, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing byte arrays with CollectionNBT target and type hints")
    void testByteArraysCollectionNBTWithHint() {
        qt().forAll(lists().of(allBytes()).ofSizeBetween(0, 15)).as(ByteArrayNBT::new)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, CollectionNBT.class, true));
    }

    // ===================
    // Integer Arrays
    // ===================
    private boolean checkSameIntArray(List<Integer> ints, @Nullable Class<? extends INBT> targetClass) {
        IntArrayNBT nbt = new IntArrayNBT(ints);
        Object sanitized = CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, targetClass, false);
        if (ints.isEmpty()) {
            if (targetClass == INBT.class) {
                return sanitized instanceof CompoundNBT && ((CompoundNBT) sanitized).isEmpty();
            }//CollectionNBT
            return sanitized instanceof ListNBT && ((ListNBT) sanitized).isEmpty();
        } else if (ints.stream().allMatch(value -> value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE)) {
            ByteArrayNBT expected = new ByteArrayNBT(ints.stream().map(Integer::byteValue).collect(Collectors.toList()));
            return expected.equals(sanitized);
        } else if (ints.stream().allMatch(value -> value >= Short.MIN_VALUE && value <= Short.MAX_VALUE)) {
            ListNBT expected = ints.stream().map(i -> ShortNBT.valueOf(i.shortValue())).collect(Collectors.toCollection(ListNBT::new));
            return expected.equals(sanitized);
        }
        return nbt.equals(sanitized);
    }

    @Test
    @DisplayName("Test serializing and deserializing int arrays with IntArrayNBT target and no hints")
    void testIntArrays() {
        qt().forAll(lists().of(integers().all()).ofSizeBetween(0, 15)).as(IntArrayNBT::new)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing int arrays with INBT target and no hints")
    void testIntArraysINBT() {
        qt().forAll(lists().of(integers().all()).ofSizeBetween(0, 15)).check(ints -> checkSameIntArray(ints, INBT.class));
    }

    @Test
    @DisplayName("Test serializing and deserializing int arrays with CollectionNBT target and no hints")
    void testIntArraysCollectionNBT() {
        qt().forAll(lists().of(integers().all()).ofSizeBetween(0, 15)).check(ints -> checkSameIntArray(ints, CollectionNBT.class));
    }

    @Test
    @DisplayName("Test serializing and deserializing int arrays with IntArrayNBT target and type hints")
    void testIntArraysWithHint() {
        qt().forAll(lists().of(integers().all()).ofSizeBetween(0, 15)).as(IntArrayNBT::new)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing int arrays with INBT target and type hints")
    void testIntArraysINBTWithHint() {
        qt().forAll(lists().of(integers().all()).ofSizeBetween(0, 15)).as(IntArrayNBT::new)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, INBT.class, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing int arrays with CollectionNBT target and type hints")
    void testIntArraysCollectionNBTWithHint() {
        qt().forAll(lists().of(integers().all()).ofSizeBetween(0, 15)).as(IntArrayNBT::new)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, CollectionNBT.class, true));
    }

    // ===================
    // Long Arrays
    // ===================
    private boolean checkSameLongArray(List<Long> longs, @Nullable Class<? extends INBT> targetClass) {
        LongArrayNBT nbt = new LongArrayNBT(longs);
        Object sanitized = CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, targetClass, false);
        if (longs.isEmpty()) {
            if (targetClass == INBT.class) {
                return sanitized instanceof CompoundNBT && ((CompoundNBT) sanitized).isEmpty();
            }//CollectionNBT
            return sanitized instanceof ListNBT && ((ListNBT) sanitized).isEmpty();
        } else if (longs.stream().allMatch(value -> value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE)) {
            ByteArrayNBT expected = new ByteArrayNBT(longs.stream().map(Long::byteValue).collect(Collectors.toList()));
            return expected.equals(sanitized);
        } else if (longs.stream().allMatch(value -> value >= Short.MIN_VALUE && value <= Short.MAX_VALUE)) {
            ListNBT expected = longs.stream().map(i -> ShortNBT.valueOf(i.shortValue())).collect(Collectors.toCollection(ListNBT::new));
            return expected.equals(sanitized);
        } else if (longs.stream().allMatch(value -> value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE)) {
            IntArrayNBT expected = new IntArrayNBT(longs.stream().map(Long::intValue).collect(Collectors.toList()));
            return expected.equals(sanitized);
        }
        return getExpected(longs).equals(sanitized);
    }

    private LongArrayNBT getExpected(List<Long> longs) {
        //Adjust for floating point accuracy as needed
        if (longs.stream().anyMatch(value -> value != (long) (double) value)) {
            return new LongArrayNBT(longs.stream().map(v -> (long) (double) v).collect(Collectors.toList()));
        }
        return new LongArrayNBT(longs);
    }

    private boolean checkSameLongArray(List<Long> longs, @Nullable Class<? extends INBT> targetClass, boolean includeHints) {
        LongArrayNBT nbt = new LongArrayNBT(longs);
        Object sanitized = CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, targetClass, includeHints);
        return getExpected(longs).equals(sanitized);
    }

    @Test
    @DisplayName("Test serializing and deserializing long arrays with LongArrayNBT target and no hints")
    void testLongArrays() {
        qt().forAll(lists().of(longs().all()).ofSizeBetween(0, 15)).check(longs -> checkSameLongArray(longs, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing long arrays with INBT target and no hints")
    void testLongArraysINBT() {
        qt().forAll(lists().of(longs().all()).ofSizeBetween(0, 15)).check(longs -> checkSameLongArray(longs, INBT.class));
    }

    @Test
    @DisplayName("Test serializing and deserializing long arrays with CollectionNBT target and no hints")
    void testLongArraysCollectionNBT() {
        qt().forAll(lists().of(longs().all()).ofSizeBetween(0, 15)).check(longs -> checkSameLongArray(longs, CollectionNBT.class));
    }

    @Test
    @DisplayName("Test serializing and deserializing long arrays with LongArrayNBT target and type hints")
    void testLongArraysWithHint() {
        qt().forAll(lists().of(longs().all()).ofSizeBetween(0, 15)).check(longs -> checkSameLongArray(longs, null, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing long arrays with INBT target and type hints")
    void testLongArraysINBTWithHint() {
        qt().forAll(lists().of(longs().all()).ofSizeBetween(0, 15)).check(longs -> checkSameLongArray(longs, INBT.class, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing long arrays with CollectionNBT target and type hints")
    void testLongArraysCollectionNBTWithHint() {
        qt().forAll(lists().of(longs().all()).ofSizeBetween(0, 15)).check(longs -> checkSameLongArray(longs, CollectionNBT.class, true));
    }

    // ===================
    // Lists
    // ===================
    private ListNBT fromArray(INBT... elements) {
        ListNBT nbt = new ListNBT();
        Collections.addAll(nbt, elements);
        return nbt;
    }

    private ListNBT fromBytes(List<Byte> bytes) {
        return bytes.stream().map(ByteNBT::valueOf).collect(Collectors.toCollection(ListNBT::new));
    }

    private ListNBT fromShorts(List<Short> shorts) {
        return shorts.stream().map(ShortNBT::valueOf).collect(Collectors.toCollection(ListNBT::new));
    }

    private ListNBT fromInts(List<Integer> ints) {
        return ints.stream().map(IntNBT::valueOf).collect(Collectors.toCollection(ListNBT::new));
    }

    private ListNBT fromLongs(List<Long> longs) {
        return longs.stream().map(LongNBT::valueOf).collect(Collectors.toCollection(ListNBT::new));
    }

    private ListNBT fromStrings(List<String> strings) {
        return strings.stream().map(StringNBT::valueOf).collect(Collectors.toCollection(ListNBT::new));
    }

    @Test
    @DisplayName("Test serializing and deserializing string lists with ListNBT target and no hints")
    void testStringList() {
        qt().forAll(lists().of(strings().ascii().ofLengthBetween(0, 15)).ofSizeBetween(1, 15)).as(this::fromStrings)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing string lists with INBT target and no hints")
    void testStringListINBT() {
        qt().forAll(lists().of(strings().ascii().ofLengthBetween(0, 15)).ofSizeBetween(1, 15)).as(this::fromStrings)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, INBT.class, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing string lists with CollectionNBT target and no hints")
    void testStringListCollectionNBT() {
        qt().forAll(lists().of(strings().ascii().ofLengthBetween(0, 15)).ofSizeBetween(1, 15)).as(this::fromStrings)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, CollectionNBT.class, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing string lists with ListNBT target and type hints")
    void testStringListWithHint() {
        qt().forAll(lists().of(strings().ascii().ofLengthBetween(0, 15)).ofSizeBetween(1, 15)).as(this::fromStrings)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing string lists with INBT target and type hints")
    void testStringListINBTWithHint() {
        qt().forAll(lists().of(strings().ascii().ofLengthBetween(0, 15)).ofSizeBetween(1, 15)).as(this::fromStrings)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, INBT.class, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing string lists with CollectionNBT target and type hints")
    void testStringListCollectionNBTWithHint() {
        qt().forAll(lists().of(strings().ascii().ofLengthBetween(0, 15)).ofSizeBetween(1, 15)).as(this::fromStrings)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, CollectionNBT.class, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing lists that would be decoded as a byte plus a short")
    void testListByteToShort() {
        qt().forAll(
                    allBytes(),
                    onlyShorts()
              ).as((b, s) -> fromArray(ShortNBT.valueOf(b), ShortNBT.valueOf(s)))
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing lists that would be decoded as a byte plus an int")
    void testListByteToInt() {
        qt().forAll(
                    allBytes(),
                    onlyInts()
              ).as((b, i) -> fromArray(IntNBT.valueOf(b), IntNBT.valueOf(i)))
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing lists that would be decoded as a byte plus a long")
    void testListByteToLong() {
        qt().forAll(
              allBytes(),
              onlyLongs()
        ).check((b, l) -> {
            ListNBT nbt = fromArray(LongNBT.valueOf(b), LongNBT.valueOf(l));
            ListNBT expected = fromArray(LongNBT.valueOf(b), getExpected(l));
            return expected.equals(CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, null, false));
        });
    }

    @Test
    @DisplayName("Test serializing and deserializing lists that would be decoded as a byte plus a float")
    void testListByteToFloat() {
        qt().forAll(allBytes()).as(b -> fromArray(FloatNBT.valueOf(b), FloatNBT.valueOf(Float.MAX_VALUE)))
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing lists that would be decoded as a byte plus a double")
    void testListByteToDouble() {
        qt().forAll(allBytes()).as(b -> fromArray(DoubleNBT.valueOf(b), DoubleNBT.valueOf(Double.MAX_VALUE)))
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing lists that would be decoded as a short plus an int")
    void testListShortToInt() {
        qt().forAll(
                    onlyShorts(),
                    onlyInts()
              ).as((s, i) -> fromArray(IntNBT.valueOf(s), IntNBT.valueOf(i)))
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing lists that would be decoded as a short plus a long")
    void testListShortToLong() {
        qt().forAll(
              onlyShorts(),
              onlyLongs()
        ).check((s, l) -> {
            ListNBT nbt = fromArray(LongNBT.valueOf(s), LongNBT.valueOf(l));
            ListNBT expected = fromArray(LongNBT.valueOf(s), getExpected(l));
            return expected.equals(CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, null, false));
        });
    }

    @Test
    @DisplayName("Test serializing and deserializing lists that would be decoded as a short plus a float")
    void testListShortToFloat() {
        qt().forAll(onlyShorts()).as(s -> fromArray(FloatNBT.valueOf(s), FloatNBT.valueOf(Float.MAX_VALUE)))
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing lists that would be decoded as a short plus a double")
    void testListShortToDouble() {
        qt().forAll(onlyShorts()).as(s -> fromArray(DoubleNBT.valueOf(s), DoubleNBT.valueOf(Double.MAX_VALUE)))
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing lists that would be decoded as an int plus a long")
    void testListIntToLong() {
        qt().forAll(
              onlyInts(),
              onlyLongs()
        ).check((i, l) -> {
            ListNBT nbt = fromArray(LongNBT.valueOf(i), LongNBT.valueOf(l));
            ListNBT expected = fromArray(LongNBT.valueOf(i), getExpected(l));
            return expected.equals(CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, null, false));
        });
    }

    @Test
    @DisplayName("Test serializing and deserializing lists that would be decoded as an int plus a float")
    void testListIntToFloat() {
        qt().forAll(onlyInts()).as(i -> fromArray(FloatNBT.valueOf(i), FloatNBT.valueOf(Float.MAX_VALUE)))
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing lists that would be decoded as an int plus a double")
    void testListIntToDouble() {
        qt().forAll(onlyInts()).as(i -> fromArray(DoubleNBT.valueOf(i), DoubleNBT.valueOf(Double.MAX_VALUE)))
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing lists that would be decoded as a long plus a float")
    void testListLongToFloat() {
        qt().forAll(onlyLongs()).as(l -> fromArray(FloatNBT.valueOf(l), FloatNBT.valueOf(Float.MAX_VALUE)))
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing lists that would be decoded as a long plus a double")
    void testListLongToDouble() {
        qt().forAll(onlyLongs()).as(l -> fromArray(DoubleNBT.valueOf(l), DoubleNBT.valueOf(Double.MAX_VALUE)))
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing lists that would be decoded as a float plus a double")
    void testListFloatToDouble() {
        qt().forAll(floats().between(Long.MAX_VALUE + 1F, Float.MAX_VALUE)).as(f -> fromArray(DoubleNBT.valueOf(f), DoubleNBT.valueOf(Double.MAX_VALUE)))
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing lists of lists that would create the inner ones as a byte array plus short list")
    void testListByteArrayToShortList() {
        qt().forAll(
                    lists().of(allBytes()).ofSizeBetween(1, 15),
                    lists().of(onlyShorts()).ofSizeBetween(1, 15)
              ).as((bytes, shorts) -> fromArray(fromBytes(bytes), fromShorts(shorts)))
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing lists of lists that would create the inner ones as a byte array plus int array")
    void testListByteArrayToIntArray() {
        qt().forAll(
              lists().of(allBytes()).ofSizeBetween(1, 15),
              lists().of(onlyInts()).ofSizeBetween(1, 15)
        ).check((bytes, ints) -> {
            ListNBT nbt = fromArray(fromBytes(bytes), fromInts(ints));
            ListNBT expected = fromArray(new IntArrayNBT(bytes.stream().mapToInt(Number::intValue).toArray()), new IntArrayNBT(ints));
            return expected.equals(CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, null, false));
        });
    }

    @Test
    @DisplayName("Test serializing and deserializing lists of lists that would create the inner ones as a byte array plus long array")
    void testListByteArrayToLongArray() {
        qt().forAll(
              lists().of(allBytes()).ofSizeBetween(1, 15),
              lists().of(onlyLongs()).ofSizeBetween(1, 15)
        ).check((bytes, longs) -> {
            ListNBT nbt = fromArray(fromBytes(bytes), fromLongs(longs));
            ListNBT expected = fromArray(new LongArrayNBT(bytes.stream().mapToLong(Number::longValue).toArray()), getExpected(longs));
            return expected.equals(CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, null, false));
        });
    }

    @Test
    @DisplayName("Test serializing and deserializing lists of lists that would create the inner ones as a short list plus int array")
    void testListShortListToIntArray() {
        qt().forAll(
              lists().of(onlyShorts()).ofSizeBetween(1, 15),
              lists().of(onlyInts()).ofSizeBetween(1, 15)
        ).check((shorts, ints) -> {
            ListNBT nbt = fromArray(fromShorts(shorts), fromInts(ints));
            ListNBT expected = fromArray(new IntArrayNBT(shorts.stream().mapToInt(Number::intValue).toArray()), new IntArrayNBT(ints));
            return expected.equals(CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, null, false));
        });
    }

    @Test
    @DisplayName("Test serializing and deserializing lists of lists that would create the inner ones as a short list plus long array")
    void testListShortListToLongArray() {
        qt().forAll(
              lists().of(onlyShorts()).ofSizeBetween(1, 15),
              lists().of(onlyLongs()).ofSizeBetween(1, 15)
        ).check((shorts, longs) -> {
            ListNBT nbt = fromArray(fromShorts(shorts), fromLongs(longs));
            ListNBT expected = fromArray(new LongArrayNBT(shorts.stream().mapToLong(Number::longValue).toArray()), getExpected(longs));
            return expected.equals(CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, null, false));
        });
    }

    @Test
    @DisplayName("Test serializing and deserializing lists of lists that would create the inner ones as an int array plus long array")
    void testListIntArrayToLongArray() {
        qt().forAll(
              lists().of(onlyInts()).ofSizeBetween(1, 15),
              lists().of(onlyLongs()).ofSizeBetween(1, 15)
        ).check((ints, longs) -> {
            ListNBT nbt = fromArray(fromInts(ints), fromLongs(longs));
            ListNBT expected = fromArray(new LongArrayNBT(ints.stream().mapToLong(Number::longValue).toArray()), getExpected(longs));
            return expected.equals(CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, null, false));
        });
    }

    @Test
    @DisplayName("Test serializing and deserializing lists of lists that would create the inner ones as byte arrays plus an empty list")
    void testListByteArrayPlusEmpty() {
        qt().forAll(lists().of(allBytes()).ofSizeBetween(1, 15)).check(bytes -> {
            ListNBT nbt = fromArray(fromBytes(bytes), new ListNBT());
            ListNBT expected = fromArray(new ByteArrayNBT(bytes), new ByteArrayNBT(new byte[0]));
            return expected.equals(CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, null, false));
        });
    }

    @Test
    @DisplayName("Test serializing and deserializing lists of lists that would create the inner ones as an empty compound plus a byte array")
    void testListEmptyPlusByteArray() {
        qt().forAll(lists().of(allBytes()).ofSizeBetween(1, 15)).check(bytes -> {
            ListNBT nbt = fromArray(new ListNBT(), fromBytes(bytes));
            ListNBT expected = fromArray(new ByteArrayNBT(new byte[0]), new ByteArrayNBT(bytes));
            return expected.equals(CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, null, false));
        });
    }

    @Test
    @DisplayName("Test serializing and deserializing lists of lists that would create the inner ones as int arrays plus an empty list")
    void testListIntArrayPlusEmpty() {
        qt().forAll(lists().of(onlyInts()).ofSizeBetween(1, 15)).check(ints -> {
            ListNBT nbt = fromArray(fromInts(ints), new ListNBT());
            ListNBT expected = fromArray(new IntArrayNBT(ints), new IntArrayNBT(new int[0]));
            return expected.equals(CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, null, false));
        });
    }

    @Test
    @DisplayName("Test serializing and deserializing lists of lists that would create the inner ones as an empty compound plus an int array")
    void testListEmptyPlusIntArray() {
        qt().forAll(lists().of(onlyInts()).ofSizeBetween(1, 15)).check(ints -> {
            ListNBT nbt = fromArray(new ListNBT(), fromInts(ints));
            ListNBT expected = fromArray(new IntArrayNBT(new int[0]), new IntArrayNBT(ints));
            return expected.equals(CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, null, false));
        });
    }

    @Test
    @DisplayName("Test serializing and deserializing lists of lists that would create the inner ones as long arrays plus an empty list")
    void testListLongArrayPlusEmpty() {
        qt().forAll(lists().of(onlyLongs()).ofSizeBetween(1, 15)).check(longs -> {
            ListNBT nbt = fromArray(fromLongs(longs), new ListNBT());
            ListNBT expected = fromArray(getExpected(longs), new LongArrayNBT(new long[0]));
            return expected.equals(CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, null, false));
        });
    }

    @Test
    @DisplayName("Test serializing and deserializing lists of lists that would create the inner ones as an empty compound plus a long array")
    void testListEmptyPlusLongArray() {
        qt().forAll(lists().of(onlyLongs()).ofSizeBetween(1, 15)).check(longs -> {
            ListNBT nbt = fromArray(new ListNBT(), fromLongs(longs));
            ListNBT expected = fromArray(new LongArrayNBT(new long[0]), getExpected(longs));
            return expected.equals(CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, null, false));
        });
    }

    @Test
    @DisplayName("Test serializing and deserializing lists of lists that would create the inner ones as an empty compound plus some arbitrary list type (strings)")
    void testListEmptyPlusList() {
        qt().forAll(lists().of(strings().ascii().ofLengthBetween(0, 15)).ofSizeBetween(1, 15)).check(strings -> {
            ListNBT nbt = fromArray(new ListNBT(), fromStrings(strings));
            return CCArgumentWrapperTestHelper.checkSame(nbt, null, false);
        });
    }

    @Test
    @DisplayName("Test serializing and deserializing lists of lists that would create the inner ones as a byte array plus some arbitrary list type (strings)")
    void testListByteArrayToList() {
        qt().forAll(
                    lists().of(allBytes()).ofSizeBetween(1, 15),
                    lists().of(strings().ascii().ofLengthBetween(0, 15)).ofSizeBetween(1, 15)
              ).as((bytes, strings) -> fromArray(fromBytes(bytes), fromStrings(strings)))
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing lists of lists that would create the inner ones as an int array plus some arbitrary list type (strings)")
    void testListIntArrayToList() {
        qt().forAll(
                    lists().of(onlyInts()).ofSizeBetween(1, 15),
                    lists().of(strings().ascii().ofLengthBetween(0, 15)).ofSizeBetween(1, 15)
              ).as((ints, strings) -> fromArray(fromInts(ints), fromStrings(strings)))
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing lists of lists that would create the inner ones as a long array plus some arbitrary list type (strings)")
    void testListLongArrayToList() {
        qt().forAll(
              lists().of(onlyLongs()).ofSizeBetween(1, 15),
              lists().of(strings().ascii().ofLengthBetween(0, 15)).ofSizeBetween(1, 15)
        ).check((longs, strings) -> {
            ListNBT stringListNBT = fromStrings(strings);
            ListNBT nbt = fromArray(fromLongs(longs), stringListNBT);
            ListNBT expectedLongs;
            if (longs.stream().anyMatch(value -> value != (long) (double) value)) {
                expectedLongs = longs.stream().map(v -> LongNBT.valueOf((long) (double) v)).collect(Collectors.toCollection(ListNBT::new));
            } else {
                expectedLongs = longs.stream().map(LongNBT::valueOf).collect(Collectors.toCollection(ListNBT::new));
            }
            ListNBT expected = fromArray(expectedLongs, stringListNBT);
            return expected.equals(CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, null, false));
        });
    }

    @Test
    @DisplayName("Test serializing and deserializing lists of lists that would create the inner ones as a short list plus some arbitrary list type (strings)")
    void testListShortListToList() {
        qt().forAll(
                    lists().of(onlyShorts()).ofSizeBetween(1, 15),
                    lists().of(strings().ascii().ofLengthBetween(0, 15)).ofSizeBetween(1, 15)
              ).as((shorts, strings) -> fromArray(fromShorts(shorts), fromStrings(strings)))
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing lists of lists that would create the inner ones as some arbitrary list type (strings) and a short list")
    void testListListToList() {
        qt().forAll(
                    lists().of(strings().ascii().ofLengthBetween(0, 15)).ofSizeBetween(1, 15),
                    lists().of(onlyShorts()).ofSizeBetween(1, 15)
              ).as((strings, shorts) -> fromArray(fromStrings(strings), fromShorts(shorts)))
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }
}