package mekanism.common.integration.computer;

import net.minecraft.nbt.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@DisplayName("Test Special Converters")
class SpecialConvertersWrapperTest {

    // ===================
    // EndTag
    // ===================
    @Test
    @DisplayName("Test serializing and deserializing EndTag with EndTag target and no hints")
    void testEnd() {
        SpecialConvertersTestHelper.assertSame(EndTag.INSTANCE, null, false);
    }

    @Test
    @DisplayName("Test serializing and deserializing EndTag with Tag target and no hints")
    void testEndTag() {
        Object sanitized = SpecialConvertersTestHelper.wrapAndSanitize(EndTag.INSTANCE, Tag.class, false);
        Assertions.assertEquals(StringTag.valueOf(EndTag.INSTANCE.toString()), sanitized);
    }

    @Test
    @DisplayName("Test serializing and deserializing EndTag with EndTag target and type hints")
    void testEndWithHint() {
        SpecialConvertersTestHelper.assertSame(EndTag.INSTANCE, null, true);
    }

    @Test
    @DisplayName("Test serializing and deserializing EndTag with Tag target and type hints")
    void testEndTagWithHint() {
        SpecialConvertersTestHelper.assertSame(EndTag.INSTANCE, Tag.class, true);
    }

    // ===================
    // CompoundTag
    // ===================
    @Test
    @DisplayName("Test serializing and deserializing empty Compounds with CompoundTag target and no hints")
    void testEmptyCompound() {
        SpecialConvertersTestHelper.assertSame(new CompoundTag(), null, false);
    }

    @Test
    @DisplayName("Test serializing and deserializing empty Compounds with Tag target and no hints")
    void testEmptyCompoundTag() {
        SpecialConvertersTestHelper.assertSame(new CompoundTag(), Tag.class, false);
    }

    @Test
    @DisplayName("Test serializing and deserializing empty Compounds with CompoundTag target and type hints")
    void testEmptyCompoundWithHint() {
        SpecialConvertersTestHelper.assertSame(new CompoundTag(), null, true);
    }

    @Test
    @DisplayName("Test serializing and deserializing empty Compounds with Tag target and type hints")
    void testEmptyCompoundTagWithHint() {
        SpecialConvertersTestHelper.assertSame(new CompoundTag(), Tag.class, true);
    }

    @Test
    @DisplayName("Test serializing and deserializing simple Compounds with CompoundTag target and no hints")
    void testCompound() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("test", "value");
        SpecialConvertersTestHelper.assertSame(nbt, null, false);
    }

    @Test
    @DisplayName("Test serializing and deserializing simple Compounds with Tag target and no hints")
    void testCompoundTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("test", "value");
        SpecialConvertersTestHelper.assertSame(nbt, Tag.class, false);
    }

    @Test
    @DisplayName("Test serializing and deserializing simple Compounds with CompoundTag target and type hints")
    void testCompoundWithHint() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("test", "value");
        SpecialConvertersTestHelper.assertSame(nbt, null, true);
    }

    @Test
    @DisplayName("Test serializing and deserializing simple Compounds with Tag target and type hints")
    void testCompoundTagWithHint() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("test", "value");
        SpecialConvertersTestHelper.assertSame(nbt, Tag.class, true);
    }

    @Test
    @DisplayName("Test serializing and deserializing Compound with a key corresponding to the type hint with CompoundTag target and no hints")
    void testCompoundWithTypeHintAsKey() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("test", "value");
        nbt.putString(SpecialConverters.TYPE_HINT_KEY, "fakeHint");
        SpecialConvertersTestHelper.assertSame(nbt, null, false);
    }

    // ===================
    // Lists (the majority of list testing is done via property tests)
    // ===================
    @Test
    @DisplayName("Test serializing and deserializing empty lists with ListTag target and no hints")
    void testEmptyList() {
        SpecialConvertersTestHelper.assertSame(new ListTag(), null, false);
    }

    @Test
    @DisplayName("Test serializing and deserializing empty lists with Tag target and no hints")
    void testEmptyListTag() {
        Object sanitized = SpecialConvertersTestHelper.wrapAndSanitize(new ListTag(), Tag.class, false);
        Assertions.assertEquals(new CompoundTag(), sanitized);
    }

    @Test
    @DisplayName("Test serializing and deserializing empty lists with CollectionTag target and no hints")
    void testEmptyListCollectionTag() {
        SpecialConvertersTestHelper.assertSame(new ListTag(), CollectionTag.class, false);
    }

    @Test
    @DisplayName("Test serializing and deserializing empty lists with ListTag target and type hints")
    void testEmptyListWithHint() {
        SpecialConvertersTestHelper.assertSame(new ListTag(), null, true);
    }

    @Test
    @DisplayName("Test serializing and deserializing empty lists with Tag target and type hints")
    void testEmptyListTagWithHint() {
        SpecialConvertersTestHelper.assertSame(new ListTag(), Tag.class, true);
    }

    @Test
    @DisplayName("Test serializing and deserializing empty lists with CollectionTag target and type hints")
    void testEmptyListCollectionTagWithHint() {
        SpecialConvertersTestHelper.assertSame(new ListTag(), CollectionTag.class, true);
    }

    // ===================
    // Invalid Hints
    // ===================
    private Object addInvalidHint(Tag nbt, int type) {
        //Validate that we aren't accidentally providing a valid hint
        Assertions.assertNotEquals(nbt.getId(), type);
        Map<Object, Object> hint = new HashMap<>(2);
        hint.put(SpecialConverters.TYPE_HINT_KEY, (double) type);
        hint.put(SpecialConverters.TYPE_HINT_VALUE_KEY, SpecialConvertersTestHelper.wrapTag(nbt, false));
        return hint;
    }

    @Test
    @DisplayName("Test hint mismatch expected type using numbers as actual")
    void testInvalidHintNumber() {
        IntTag nbt = IntTag.valueOf(100);
        Object withHint = addInvalidHint(nbt, Tag.TAG_COMPOUND);
        Assertions.assertNotEquals(nbt, SpecialConvertersTestHelper.sanitize(nbt.getClass(), withHint));
    }

    @Test
    @DisplayName("Test hint mismatch expected type using compounds as actual")
    void testInvalidHintCompound() {
        CompoundTag nbt = new CompoundTag();
        Object withHint = addInvalidHint(nbt, Tag.TAG_INT_ARRAY);
        Assertions.assertNotEquals(nbt, SpecialConvertersTestHelper.sanitize(nbt.getClass(), withHint));
    }

    @Test
    @DisplayName("Test hint mismatch expected type using arrays as actual")
    void testInvalidHintCollection() {
        ByteArrayTag nbt = new ByteArrayTag(new byte[0]);
        Object withHint = addInvalidHint(nbt, Tag.TAG_FLOAT);
        Assertions.assertNotEquals(nbt, SpecialConvertersTestHelper.sanitize(nbt.getClass(), withHint));
    }

    // ===================
    // Invalid Lists
    // ===================
    private ListTag createStringList(String... strings) {
        return Arrays.stream(strings).map(StringTag::valueOf).collect(Collectors.toCollection(ListTag::new));
    }

    private void assertMismatchedList(Tag... mismatchedElements) {
        //Ensure we have some elements in the list
        Assertions.assertTrue(mismatchedElements.length > 1);
        Map<Double, Object> wrappedCollection = new HashMap<>(mismatchedElements.length);
        for (int i = 0; i < mismatchedElements.length; i++) {
            wrappedCollection.put((double) i, SpecialConvertersTestHelper.wrapTag(mismatchedElements[i], false));
        }
        Assertions.assertFalse(SpecialConvertersTestHelper.sanitize(ListTag.class, wrappedCollection) instanceof ListTag);
    }

    @Test
    @DisplayName("Test making sure we fail to create a list out of invalid elements (number + string)")
    void testInvalidListNumberString() {
        assertMismatchedList(IntTag.valueOf(100), StringTag.valueOf("Test"));
    }

    @Test
    @DisplayName("Test making sure we fail to create a list out of invalid elements (string + number)")
    void testInvalidListStringNumber() {
        assertMismatchedList(StringTag.valueOf("Test"), IntTag.valueOf(100));
    }

    @Test
    @DisplayName("Test making sure we fail to create a list out of invalid elements (string + byte array)")
    void testInvalidListStringByteArray() {
        assertMismatchedList(StringTag.valueOf("Test"), new ByteArrayTag(Collections.singletonList((byte) 0)));
    }

    @Test
    @DisplayName("Test making sure we fail to create a list out of invalid elements (string + int array)")
    void testInvalidListStringIntArray() {
        assertMismatchedList(StringTag.valueOf("Test"), new IntArrayTag(Collections.singletonList(Integer.MAX_VALUE)));
    }

    @Test
    @DisplayName("Test making sure we fail to create a list out of invalid elements (string + long array)")
    void testInvalidListStringLongArray() {
        assertMismatchedList(StringTag.valueOf("Test"), new LongArrayTag(Collections.singletonList(Long.MAX_VALUE)));
    }

    @Test
    @DisplayName("Test making sure we fail to create a list out of invalid elements (non empty compound + byte array)")
    void testInvalidListCompoundByteArray() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("key", "value");
        assertMismatchedList(nbt, new ByteArrayTag(Collections.singletonList((byte) 0)));
    }

    @Test
    @DisplayName("Test making sure we fail to create a list out of invalid elements (non empty compound + int array)")
    void testInvalidListCompoundIntArray() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("key", "value");
        assertMismatchedList(nbt, new IntArrayTag(Collections.singletonList(Integer.MAX_VALUE)));
    }

    @Test
    @DisplayName("Test making sure we fail to create a list out of invalid elements (non empty compound + long array)")
    void testInvalidListCompoundLongArray() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("key", "value");
        assertMismatchedList(nbt, new LongArrayTag(Collections.singletonList(Long.MAX_VALUE)));
    }

    @Test
    @DisplayName("Test making sure we fail to create a list out of invalid elements (non empty compound + arbitrary list type (strings))")
    void testInvalidListCompoundStringList() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("key", "value");
        assertMismatchedList(nbt, createStringList("A", "B", "C"));
    }

    @Test
    @DisplayName("Test making sure we fail to create a list out of invalid elements (string + arbitrary list type (strings))")
    void testInvalidListStringStringList() {
        assertMismatchedList(StringTag.valueOf("Test"), createStringList("A", "B", "C"));
    }
}