package mekanism.common.integration.computer.computercraft;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;
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
    @DisplayName("Test serializing and deserializing bytes with ByteTag target and no hints")
    void testBytes() {
        qt().forAll(allBytes()).as(ByteTag::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing bytes with Tag target and no hints")
    void testBytesTag() {
        qt().forAll(allBytes()).as(ByteTag::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, Tag.class, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing bytes with NumericTag target and no hints")
    void testBytesNumericTag() {
        qt().forAll(allBytes()).as(ByteTag::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, NumericTag.class, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing bytes with ByteTag target and type hints")
    void testBytesWithHint() {
        qt().forAll(allBytes()).as(ByteTag::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing bytes with Tag target and type hints")
    void testBytesTagWithHint() {
        qt().forAll(allBytes()).as(ByteTag::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, Tag.class, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing bytes with NumericTag target and type hints")
    void testBytesNumericTagWithHint() {
        qt().forAll(allBytes()).as(ByteTag::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, NumericTag.class, true));
    }

    // ===================
    // Shorts
    // ===================
    private boolean checkSameShort(short value, @Nullable Class<? extends Tag> targetClass) {
        ShortTag nbt = ShortTag.valueOf(value);
        Object sanitized = CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, targetClass, false);
        if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
            return nbt.equals(sanitized);
        }
        return ByteTag.valueOf((byte) value).equals(sanitized);
    }

    @Test
    @DisplayName("Test serializing and deserializing shorts with ShortTag target and no hints")
    void testShorts() {
        qt().forAll(allShorts()).as(ShortTag::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing shorts with Tag target and no hints")
    void testShortsTag() {
        qt().forAll(allShorts()).check(value -> checkSameShort(value, Tag.class));
    }

    @Test
    @DisplayName("Test serializing and deserializing shorts with NumericTag target and no hints")
    void testShortsNumericTag() {
        qt().forAll(allShorts()).check(value -> checkSameShort(value, NumericTag.class));
    }

    @Test
    @DisplayName("Test serializing and deserializing shorts with ShortTag target and type hints")
    void testShortsWithHint() {
        qt().forAll(allShorts()).as(ShortTag::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing shorts with Tag target and type hints")
    void testShortsTagWithHint() {
        qt().forAll(allShorts()).as(ShortTag::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, Tag.class, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing shorts with NumericTag target and type hints")
    void testShortsNumericTagWithHint() {
        qt().forAll(allShorts()).as(ShortTag::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, NumericTag.class, true));
    }

    // ===================
    // Ints
    // ===================
    private boolean checkSameInt(int value, @Nullable Class<? extends Tag> targetClass) {
        IntTag nbt = IntTag.valueOf(value);
        Object sanitized = CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, targetClass, false);
        if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
            return nbt.equals(sanitized);
        } else if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
            return ShortTag.valueOf((short) value).equals(sanitized);
        }
        return ByteTag.valueOf((byte) value).equals(sanitized);
    }

    @Test
    @DisplayName("Test serializing and deserializing ints with IntTag target and no hints")
    void testInts() {
        qt().forAll(integers().all()).as(IntTag::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing ints with Tag target and no hints")
    void testIntsTag() {
        qt().forAll(integers().all()).check(value -> checkSameInt(value, Tag.class));
    }

    @Test
    @DisplayName("Test serializing and deserializing ints with NumericTag target and no hints")
    void testIntsNumericTag() {
        qt().forAll(integers().all()).check(value -> checkSameInt(value, NumericTag.class));
    }

    @Test
    @DisplayName("Test serializing and deserializing ints with IntTag target and type hints")
    void testIntsWithHint() {
        qt().forAll(integers().all()).as(IntTag::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing ints with Tag target and type hints")
    void testIntsTagWithHint() {
        qt().forAll(integers().all()).as(IntTag::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, Tag.class, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing ints with NumericTag target and type hints")
    void testIntsNumericTagWithHint() {
        qt().forAll(integers().all()).as(IntTag::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, NumericTag.class, true));
    }

    // ===================
    // Longs
    // ===================
    private LongTag getExpected(long l) {
        //Adjust for floating point accuracy as needed
        if (l != (long) (double) l) {
            return LongTag.valueOf((long) (double) l);
        }
        return LongTag.valueOf(l);
    }

    private boolean checkSameLong(long value, @Nullable Class<? extends Tag> targetClass) {
        LongTag nbt = LongTag.valueOf(value);
        Object sanitized = CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, targetClass, false);
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            return getExpected(value).equals(sanitized);
        } else if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
            return IntTag.valueOf((int) value).equals(sanitized);
        } else if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
            return ShortTag.valueOf((short) value).equals(sanitized);
        }
        return ByteTag.valueOf((byte) value).equals(sanitized);
    }

    private boolean checkSameLong(long value, @Nullable Class<? extends Tag> targetClass, boolean includeHints) {
        LongTag nbt = LongTag.valueOf(value);
        Object sanitized = CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, targetClass, includeHints);
        return getExpected(value).equals(sanitized);
    }

    @Test
    @DisplayName("Test serializing and deserializing longs with LongTag target and no hints")
    void testLongs() {
        qt().forAll(longs().all()).check(value -> checkSameLong(value, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing longs with Tag target and no hints")
    void testLongsTag() {
        qt().forAll(longs().all()).check(value -> checkSameLong(value, Tag.class));
    }

    @Test
    @DisplayName("Test serializing and deserializing longs with NumericTag target and no hints")
    void testLongsNumericTag() {
        qt().forAll(longs().all()).check(value -> checkSameLong(value, NumericTag.class));
    }

    @Test
    @DisplayName("Test serializing and deserializing longs with LongTag target and type hints")
    void testLongsWithHint() {
        qt().forAll(longs().all()).check(value -> checkSameLong(value, null, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing longs with Tag target and type hints")
    void testLongsTagWithHint() {
        qt().forAll(longs().all()).check(value -> checkSameLong(value, Tag.class, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing longs with NumericTag target and type hints")
    void testLongsNumericTagWithHint() {
        qt().forAll(longs().all()).check(value -> checkSameLong(value, NumericTag.class, true));
    }

    // ===================
    // Floats
    // ===================
    private boolean checkSameFloat(float value, @Nullable Class<? extends Tag> targetClass) {
        FloatTag nbt = FloatTag.valueOf(value);
        Object sanitized = CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, targetClass, false);
        if (value == Math.floor(value) && value >= Long.MIN_VALUE && value <= Long.MAX_VALUE) {
            //If we are an integer float check the proper type
            if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
                return LongTag.valueOf((long) value).equals(sanitized);
            } else if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
                return IntTag.valueOf((int) value).equals(sanitized);
            } else if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
                return ShortTag.valueOf((short) value).equals(sanitized);
            }
            return ByteTag.valueOf((byte) value).equals(sanitized);
        }
        return nbt.equals(sanitized);
    }

    @Test
    @DisplayName("Test serializing and deserializing floats with FloatTag target and no hints")
    void testFloats() {
        qt().forAll(anyFloat()).as(FloatTag::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing floats with Tag target and no hints")
    void testFloatsTag() {
        qt().forAll(anyFloat()).check(value -> checkSameFloat(value, Tag.class));
    }

    @Test
    @DisplayName("Test serializing and deserializing floats with NumericTag target and no hints")
    void testFloatsNumericTag() {
        qt().forAll(anyFloat()).check(value -> checkSameFloat(value, NumericTag.class));
    }

    @Test
    @DisplayName("Test serializing and deserializing floats with FloatTag target and type hints")
    void testFloatsWithHint() {
        qt().forAll(anyFloat()).as(FloatTag::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing floats with Tag target and type hints")
    void testFloatsTagWithHint() {
        qt().forAll(anyFloat()).as(FloatTag::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, Tag.class, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing floats with NumericTag target and type hints")
    void testFloatsNumericTagWithHint() {
        qt().forAll(anyFloat()).as(FloatTag::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, NumericTag.class, true));
    }

    // ===================
    // Doubles
    // ===================
    private boolean checkSameDouble(double value, @Nullable Class<? extends Tag> targetClass) {
        DoubleTag nbt = DoubleTag.valueOf(value);
        Object sanitized = CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, targetClass, false);
        if (value == Math.floor(value) && value >= Long.MIN_VALUE && value <= Long.MAX_VALUE) {
            //If we are an integer double-check the proper type
            if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
                return LongTag.valueOf((long) value).equals(sanitized);
            } else if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
                return IntTag.valueOf((int) value).equals(sanitized);
            } else if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
                return ShortTag.valueOf((short) value).equals(sanitized);
            }
            return ByteTag.valueOf((byte) value).equals(sanitized);
        }
        if (value < -Float.MAX_VALUE || value > Float.MAX_VALUE) {
            return nbt.equals(sanitized);
        }
        return FloatTag.valueOf((float) value).equals(sanitized);
    }

    @Test
    @DisplayName("Test serializing and deserializing doubles with DoubleTag target and no hints")
    void testDoubles() {
        qt().forAll(anyDouble()).as(DoubleTag::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing doubles with Tag target and no hints")
    void testDoublesTag() {
        qt().forAll(anyDouble()).check(value -> checkSameDouble(value, Tag.class));
    }

    @Test
    @DisplayName("Test serializing and deserializing doubles with NumericTag target and no hints")
    void testDoublesNumericTag() {
        qt().forAll(anyDouble()).check(value -> checkSameDouble(value, NumericTag.class));
    }

    @Test
    @DisplayName("Test serializing and deserializing doubles with DoubleTag target and type hints")
    void testDoublesWithHint() {
        qt().forAll(anyDouble()).as(DoubleTag::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing doubles with Tag target and type hints")
    void testDoublesTagWithHint() {
        qt().forAll(anyDouble()).as(DoubleTag::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, Tag.class, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing doubles with NumericTag target and type hints")
    void testDoublesNumericTagWithHint() {
        qt().forAll(anyDouble()).as(DoubleTag::valueOf).check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, NumericTag.class, true));
    }

    // ===================
    // Strings
    // ===================
    @Test
    @DisplayName("Test serializing and deserializing strings with StringTag target and no hints")
    void testStrings() {
        qt().forAll(strings().ascii().ofLengthBetween(0, 15)).as(StringTag::valueOf)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing strings with Tag target and no hints")
    void testStringsTag() {
        qt().forAll(strings().ascii().ofLengthBetween(0, 15)).as(StringTag::valueOf)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, Tag.class, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing strings with StringTag target and type hints")
    void testStringsWithHint() {
        qt().forAll(strings().ascii().ofLengthBetween(0, 15)).as(StringTag::valueOf)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing strings with Tag target and type hints")
    void testStringsTagWithHint() {
        qt().forAll(strings().ascii().ofLengthBetween(0, 15)).as(StringTag::valueOf)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, Tag.class, true));
    }

    // ===================
    // Byte Arrays
    // ===================
    private boolean checkSameByteArray(ByteArrayTag nbt, @Nullable Class<? extends Tag> targetClass) {
        Object sanitized = CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, targetClass, false);
        if (nbt.isEmpty()) {
            if (targetClass == Tag.class) {
                return sanitized instanceof CompoundTag compound && compound.isEmpty();
            }//CollectionTag
            return sanitized instanceof ListTag list && list.isEmpty();
        }
        return nbt.equals(sanitized);
    }

    @Test
    @DisplayName("Test serializing and deserializing byte arrays with ByteArrayTag target and no hints")
    void testByteArrays() {
        qt().forAll(lists().of(allBytes()).ofSizeBetween(0, 15)).as(ByteArrayTag::new)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing byte arrays with Tag target and no hints")
    void testByteArraysTag() {
        qt().forAll(lists().of(allBytes()).ofSizeBetween(0, 15)).as(ByteArrayTag::new).check(nbt -> checkSameByteArray(nbt, Tag.class));
    }

    @Test
    @DisplayName("Test serializing and deserializing byte arrays with CollectionTag target and no hints")
    void testByteArraysCollectionTag() {
        qt().forAll(lists().of(allBytes()).ofSizeBetween(0, 15)).as(ByteArrayTag::new).check(nbt -> checkSameByteArray(nbt, CollectionTag.class));
    }

    @Test
    @DisplayName("Test serializing and deserializing byte arrays with ByteArrayTag target and type hints")
    void testByteArraysWithHint() {
        qt().forAll(lists().of(allBytes()).ofSizeBetween(0, 15)).as(ByteArrayTag::new)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing byte arrays with Tag target and type hints")
    void testByteArraysTagWithHint() {
        qt().forAll(lists().of(allBytes()).ofSizeBetween(0, 15)).as(ByteArrayTag::new)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, Tag.class, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing byte arrays with CollectionTag target and type hints")
    void testByteArraysCollectionTagWithHint() {
        qt().forAll(lists().of(allBytes()).ofSizeBetween(0, 15)).as(ByteArrayTag::new)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, CollectionTag.class, true));
    }

    // ===================
    // Integer Arrays
    // ===================
    private boolean checkSameIntArray(List<Integer> ints, @Nullable Class<? extends Tag> targetClass) {
        IntArrayTag nbt = new IntArrayTag(ints);
        Object sanitized = CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, targetClass, false);
        if (ints.isEmpty()) {
            if (targetClass == Tag.class) {
                return sanitized instanceof CompoundTag compound && compound.isEmpty();
            }//CollectionTag
            return sanitized instanceof ListTag list && list.isEmpty();
        } else if (ints.stream().allMatch(value -> value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE)) {
            ByteArrayTag expected = new ByteArrayTag(ints.stream().map(Integer::byteValue).toList());
            return expected.equals(sanitized);
        } else if (ints.stream().allMatch(value -> value >= Short.MIN_VALUE && value <= Short.MAX_VALUE)) {
            ListTag expected = ints.stream().map(i -> ShortTag.valueOf(i.shortValue())).collect(Collectors.toCollection(ListTag::new));
            return expected.equals(sanitized);
        }
        return nbt.equals(sanitized);
    }

    @Test
    @DisplayName("Test serializing and deserializing int arrays with IntArrayTag target and no hints")
    void testIntArrays() {
        qt().forAll(lists().of(integers().all()).ofSizeBetween(0, 15)).as(IntArrayTag::new)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing int arrays with Tag target and no hints")
    void testIntArraysTag() {
        qt().forAll(lists().of(integers().all()).ofSizeBetween(0, 15)).check(ints -> checkSameIntArray(ints, Tag.class));
    }

    @Test
    @DisplayName("Test serializing and deserializing int arrays with CollectionTag target and no hints")
    void testIntArraysCollectionTag() {
        qt().forAll(lists().of(integers().all()).ofSizeBetween(0, 15)).check(ints -> checkSameIntArray(ints, CollectionTag.class));
    }

    @Test
    @DisplayName("Test serializing and deserializing int arrays with IntArrayTag target and type hints")
    void testIntArraysWithHint() {
        qt().forAll(lists().of(integers().all()).ofSizeBetween(0, 15)).as(IntArrayTag::new)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing int arrays with Tag target and type hints")
    void testIntArraysTagWithHint() {
        qt().forAll(lists().of(integers().all()).ofSizeBetween(0, 15)).as(IntArrayTag::new)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, Tag.class, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing int arrays with CollectionTag target and type hints")
    void testIntArraysCollectionTagWithHint() {
        qt().forAll(lists().of(integers().all()).ofSizeBetween(0, 15)).as(IntArrayTag::new)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, CollectionTag.class, true));
    }

    // ===================
    // Long Arrays
    // ===================
    private boolean checkSameLongArray(List<Long> longs, @Nullable Class<? extends Tag> targetClass) {
        LongArrayTag nbt = new LongArrayTag(longs);
        Object sanitized = CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, targetClass, false);
        if (longs.isEmpty()) {
            if (targetClass == Tag.class) {
                return sanitized instanceof CompoundTag compound && compound.isEmpty();
            }//CollectionTag
            return sanitized instanceof ListTag list && list.isEmpty();
        } else if (longs.stream().allMatch(value -> value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE)) {
            ByteArrayTag expected = new ByteArrayTag(longs.stream().map(Long::byteValue).toList());
            return expected.equals(sanitized);
        } else if (longs.stream().allMatch(value -> value >= Short.MIN_VALUE && value <= Short.MAX_VALUE)) {
            ListTag expected = longs.stream().map(i -> ShortTag.valueOf(i.shortValue())).collect(Collectors.toCollection(ListTag::new));
            return expected.equals(sanitized);
        } else if (longs.stream().allMatch(value -> value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE)) {
            IntArrayTag expected = new IntArrayTag(longs.stream().map(Long::intValue).toList());
            return expected.equals(sanitized);
        }
        return getExpected(longs).equals(sanitized);
    }

    private LongArrayTag getExpected(List<Long> longs) {
        //Adjust for floating point accuracy as needed
        if (longs.stream().anyMatch(value -> value != (long) (double) value)) {
            return new LongArrayTag(longs.stream().map(v -> (long) (double) v).toList());
        }
        return new LongArrayTag(longs);
    }

    private boolean checkSameLongArray(List<Long> longs, @Nullable Class<? extends Tag> targetClass, boolean includeHints) {
        LongArrayTag nbt = new LongArrayTag(longs);
        Object sanitized = CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, targetClass, includeHints);
        return getExpected(longs).equals(sanitized);
    }

    @Test
    @DisplayName("Test serializing and deserializing long arrays with LongArrayTag target and no hints")
    void testLongArrays() {
        qt().forAll(lists().of(longs().all()).ofSizeBetween(0, 15)).check(longs -> checkSameLongArray(longs, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing long arrays with Tag target and no hints")
    void testLongArraysTag() {
        qt().forAll(lists().of(longs().all()).ofSizeBetween(0, 15)).check(longs -> checkSameLongArray(longs, Tag.class));
    }

    @Test
    @DisplayName("Test serializing and deserializing long arrays with CollectionTag target and no hints")
    void testLongArraysCollectionTag() {
        qt().forAll(lists().of(longs().all()).ofSizeBetween(0, 15)).check(longs -> checkSameLongArray(longs, CollectionTag.class));
    }

    @Test
    @DisplayName("Test serializing and deserializing long arrays with LongArrayTag target and type hints")
    void testLongArraysWithHint() {
        qt().forAll(lists().of(longs().all()).ofSizeBetween(0, 15)).check(longs -> checkSameLongArray(longs, null, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing long arrays with Tag target and type hints")
    void testLongArraysTagWithHint() {
        qt().forAll(lists().of(longs().all()).ofSizeBetween(0, 15)).check(longs -> checkSameLongArray(longs, Tag.class, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing long arrays with CollectionTag target and type hints")
    void testLongArraysCollectionTagWithHint() {
        qt().forAll(lists().of(longs().all()).ofSizeBetween(0, 15)).check(longs -> checkSameLongArray(longs, CollectionTag.class, true));
    }

    // ===================
    // Lists
    // ===================
    private ListTag fromArray(Tag... elements) {
        ListTag nbt = new ListTag();
        Collections.addAll(nbt, elements);
        return nbt;
    }

    private ListTag fromBytes(List<Byte> bytes) {
        return bytes.stream().map(ByteTag::valueOf).collect(Collectors.toCollection(ListTag::new));
    }

    private ListTag fromShorts(List<Short> shorts) {
        return shorts.stream().map(ShortTag::valueOf).collect(Collectors.toCollection(ListTag::new));
    }

    private ListTag fromInts(List<Integer> ints) {
        return ints.stream().map(IntTag::valueOf).collect(Collectors.toCollection(ListTag::new));
    }

    private ListTag fromLongs(List<Long> longs) {
        return longs.stream().map(LongTag::valueOf).collect(Collectors.toCollection(ListTag::new));
    }

    private ListTag fromStrings(List<String> strings) {
        return strings.stream().map(StringTag::valueOf).collect(Collectors.toCollection(ListTag::new));
    }

    @Test
    @DisplayName("Test serializing and deserializing string lists with ListTag target and no hints")
    void testStringList() {
        qt().forAll(lists().of(strings().ascii().ofLengthBetween(0, 15)).ofSizeBetween(1, 15)).as(this::fromStrings)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing string lists with Tag target and no hints")
    void testStringListTag() {
        qt().forAll(lists().of(strings().ascii().ofLengthBetween(0, 15)).ofSizeBetween(1, 15)).as(this::fromStrings)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, Tag.class, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing string lists with CollectionTag target and no hints")
    void testStringListCollectionTag() {
        qt().forAll(lists().of(strings().ascii().ofLengthBetween(0, 15)).ofSizeBetween(1, 15)).as(this::fromStrings)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, CollectionTag.class, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing string lists with ListTag target and type hints")
    void testStringListWithHint() {
        qt().forAll(lists().of(strings().ascii().ofLengthBetween(0, 15)).ofSizeBetween(1, 15)).as(this::fromStrings)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing string lists with Tag target and type hints")
    void testStringListTagWithHint() {
        qt().forAll(lists().of(strings().ascii().ofLengthBetween(0, 15)).ofSizeBetween(1, 15)).as(this::fromStrings)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, Tag.class, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing string lists with CollectionTag target and type hints")
    void testStringListCollectionTagWithHint() {
        qt().forAll(lists().of(strings().ascii().ofLengthBetween(0, 15)).ofSizeBetween(1, 15)).as(this::fromStrings)
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, CollectionTag.class, true));
    }

    @Test
    @DisplayName("Test serializing and deserializing lists that would be decoded as a byte plus a short")
    void testListByteToShort() {
        qt().forAll(
                    allBytes(),
                    onlyShorts()
              ).as((b, s) -> fromArray(ShortTag.valueOf(b), ShortTag.valueOf(s)))
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing lists that would be decoded as a byte plus an int")
    void testListByteToInt() {
        qt().forAll(
                    allBytes(),
                    onlyInts()
              ).as((b, i) -> fromArray(IntTag.valueOf(b), IntTag.valueOf(i)))
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing lists that would be decoded as a byte plus a long")
    void testListByteToLong() {
        qt().forAll(
              allBytes(),
              onlyLongs()
        ).check((b, l) -> {
            ListTag nbt = fromArray(LongTag.valueOf(b), LongTag.valueOf(l));
            ListTag expected = fromArray(LongTag.valueOf(b), getExpected(l));
            return expected.equals(CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, null, false));
        });
    }

    @Test
    @DisplayName("Test serializing and deserializing lists that would be decoded as a byte plus a float")
    void testListByteToFloat() {
        qt().forAll(allBytes()).as(b -> fromArray(FloatTag.valueOf(b), FloatTag.valueOf(Float.MAX_VALUE)))
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing lists that would be decoded as a byte plus a double")
    void testListByteToDouble() {
        qt().forAll(allBytes()).as(b -> fromArray(DoubleTag.valueOf(b), DoubleTag.valueOf(Double.MAX_VALUE)))
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing lists that would be decoded as a short plus an int")
    void testListShortToInt() {
        qt().forAll(
                    onlyShorts(),
                    onlyInts()
              ).as((s, i) -> fromArray(IntTag.valueOf(s), IntTag.valueOf(i)))
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing lists that would be decoded as a short plus a long")
    void testListShortToLong() {
        qt().forAll(
              onlyShorts(),
              onlyLongs()
        ).check((s, l) -> {
            ListTag nbt = fromArray(LongTag.valueOf(s), LongTag.valueOf(l));
            ListTag expected = fromArray(LongTag.valueOf(s), getExpected(l));
            return expected.equals(CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, null, false));
        });
    }

    @Test
    @DisplayName("Test serializing and deserializing lists that would be decoded as a short plus a float")
    void testListShortToFloat() {
        qt().forAll(onlyShorts()).as(s -> fromArray(FloatTag.valueOf(s), FloatTag.valueOf(Float.MAX_VALUE)))
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing lists that would be decoded as a short plus a double")
    void testListShortToDouble() {
        qt().forAll(onlyShorts()).as(s -> fromArray(DoubleTag.valueOf(s), DoubleTag.valueOf(Double.MAX_VALUE)))
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing lists that would be decoded as an int plus a long")
    void testListIntToLong() {
        qt().forAll(
              onlyInts(),
              onlyLongs()
        ).check((i, l) -> {
            ListTag nbt = fromArray(LongTag.valueOf(i), LongTag.valueOf(l));
            ListTag expected = fromArray(LongTag.valueOf(i), getExpected(l));
            return expected.equals(CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, null, false));
        });
    }

    @Test
    @DisplayName("Test serializing and deserializing lists that would be decoded as an int plus a float")
    void testListIntToFloat() {
        qt().forAll(onlyInts()).as(i -> fromArray(FloatTag.valueOf(i), FloatTag.valueOf(Float.MAX_VALUE)))
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing lists that would be decoded as an int plus a double")
    void testListIntToDouble() {
        qt().forAll(onlyInts()).as(i -> fromArray(DoubleTag.valueOf(i), DoubleTag.valueOf(Double.MAX_VALUE)))
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing lists that would be decoded as a long plus a float")
    void testListLongToFloat() {
        qt().forAll(onlyLongs()).as(l -> fromArray(FloatTag.valueOf(l), FloatTag.valueOf(Float.MAX_VALUE)))
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing lists that would be decoded as a long plus a double")
    void testListLongToDouble() {
        qt().forAll(onlyLongs()).as(l -> fromArray(DoubleTag.valueOf(l), DoubleTag.valueOf(Double.MAX_VALUE)))
              .check(nbt -> CCArgumentWrapperTestHelper.checkSame(nbt, null, false));
    }

    @Test
    @DisplayName("Test serializing and deserializing lists that would be decoded as a float plus a double")
    void testListFloatToDouble() {
        qt().forAll(floats().between(Long.MAX_VALUE + 1F, Float.MAX_VALUE)).as(f -> fromArray(DoubleTag.valueOf(f), DoubleTag.valueOf(Double.MAX_VALUE)))
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
            ListTag nbt = fromArray(fromBytes(bytes), fromInts(ints));
            ListTag expected = fromArray(new IntArrayTag(bytes.stream().mapToInt(Number::intValue).toArray()), new IntArrayTag(ints));
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
            ListTag nbt = fromArray(fromBytes(bytes), fromLongs(longs));
            ListTag expected = fromArray(new LongArrayTag(bytes.stream().mapToLong(Number::longValue).toArray()), getExpected(longs));
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
            ListTag nbt = fromArray(fromShorts(shorts), fromInts(ints));
            ListTag expected = fromArray(new IntArrayTag(shorts.stream().mapToInt(Number::intValue).toArray()), new IntArrayTag(ints));
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
            ListTag nbt = fromArray(fromShorts(shorts), fromLongs(longs));
            ListTag expected = fromArray(new LongArrayTag(shorts.stream().mapToLong(Number::longValue).toArray()), getExpected(longs));
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
            ListTag nbt = fromArray(fromInts(ints), fromLongs(longs));
            ListTag expected = fromArray(new LongArrayTag(ints.stream().mapToLong(Number::longValue).toArray()), getExpected(longs));
            return expected.equals(CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, null, false));
        });
    }

    @Test
    @DisplayName("Test serializing and deserializing lists of lists that would create the inner ones as byte arrays plus an empty list")
    void testListByteArrayPlusEmpty() {
        qt().forAll(lists().of(allBytes()).ofSizeBetween(1, 15)).check(bytes -> {
            ListTag nbt = fromArray(fromBytes(bytes), new ListTag());
            ListTag expected = fromArray(new ByteArrayTag(bytes), new ByteArrayTag(new byte[0]));
            return expected.equals(CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, null, false));
        });
    }

    @Test
    @DisplayName("Test serializing and deserializing lists of lists that would create the inner ones as an empty compound plus a byte array")
    void testListEmptyPlusByteArray() {
        qt().forAll(lists().of(allBytes()).ofSizeBetween(1, 15)).check(bytes -> {
            ListTag nbt = fromArray(new ListTag(), fromBytes(bytes));
            ListTag expected = fromArray(new ByteArrayTag(new byte[0]), new ByteArrayTag(bytes));
            return expected.equals(CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, null, false));
        });
    }

    @Test
    @DisplayName("Test serializing and deserializing lists of lists that would create the inner ones as int arrays plus an empty list")
    void testListIntArrayPlusEmpty() {
        qt().forAll(lists().of(onlyInts()).ofSizeBetween(1, 15)).check(ints -> {
            ListTag nbt = fromArray(fromInts(ints), new ListTag());
            ListTag expected = fromArray(new IntArrayTag(ints), new IntArrayTag(new int[0]));
            return expected.equals(CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, null, false));
        });
    }

    @Test
    @DisplayName("Test serializing and deserializing lists of lists that would create the inner ones as an empty compound plus an int array")
    void testListEmptyPlusIntArray() {
        qt().forAll(lists().of(onlyInts()).ofSizeBetween(1, 15)).check(ints -> {
            ListTag nbt = fromArray(new ListTag(), fromInts(ints));
            ListTag expected = fromArray(new IntArrayTag(new int[0]), new IntArrayTag(ints));
            return expected.equals(CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, null, false));
        });
    }

    @Test
    @DisplayName("Test serializing and deserializing lists of lists that would create the inner ones as long arrays plus an empty list")
    void testListLongArrayPlusEmpty() {
        qt().forAll(lists().of(onlyLongs()).ofSizeBetween(1, 15)).check(longs -> {
            ListTag nbt = fromArray(fromLongs(longs), new ListTag());
            ListTag expected = fromArray(getExpected(longs), new LongArrayTag(new long[0]));
            return expected.equals(CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, null, false));
        });
    }

    @Test
    @DisplayName("Test serializing and deserializing lists of lists that would create the inner ones as an empty compound plus a long array")
    void testListEmptyPlusLongArray() {
        qt().forAll(lists().of(onlyLongs()).ofSizeBetween(1, 15)).check(longs -> {
            ListTag nbt = fromArray(new ListTag(), fromLongs(longs));
            ListTag expected = fromArray(new LongArrayTag(new long[0]), getExpected(longs));
            return expected.equals(CCArgumentWrapperTestHelper.wrapAndSanitize(nbt, null, false));
        });
    }

    @Test
    @DisplayName("Test serializing and deserializing lists of lists that would create the inner ones as an empty compound plus some arbitrary list type (strings)")
    void testListEmptyPlusList() {
        qt().forAll(lists().of(strings().ascii().ofLengthBetween(0, 15)).ofSizeBetween(1, 15)).check(strings -> {
            ListTag nbt = fromArray(new ListTag(), fromStrings(strings));
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
            ListTag stringListTag = fromStrings(strings);
            ListTag nbt = fromArray(fromLongs(longs), stringListTag);
            ListTag expectedLongs;
            if (longs.stream().anyMatch(value -> value != (long) (double) value)) {
                expectedLongs = longs.stream().map(v -> LongTag.valueOf((long) (double) v)).collect(Collectors.toCollection(ListTag::new));
            } else {
                expectedLongs = longs.stream().map(LongTag::valueOf).collect(Collectors.toCollection(ListTag::new));
            }
            ListTag expected = fromArray(expectedLongs, stringListTag);
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