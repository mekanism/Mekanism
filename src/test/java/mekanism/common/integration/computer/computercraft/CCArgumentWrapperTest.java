package mekanism.common.integration.computer.computercraft;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.nbt.ByteArrayNBT;
import net.minecraft.nbt.CollectionNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.EndNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.LongArrayNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraftforge.common.util.Constants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test ComputerCraft Argument Wrapper")
class CCArgumentWrapperTest {

    // ===================
    // EndNBT
    // ===================
    @Test
    @DisplayName("Test serializing and deserializing EndNBT with EndNBT target and no hints")
    void testEnd() {
        CCArgumentWrapperTestHelper.assertSame(EndNBT.INSTANCE, null, false);
    }

    @Test
    @DisplayName("Test serializing and deserializing EndNBT with INBT target and no hints")
    void testEndINBT() {
        Object sanitized = CCArgumentWrapperTestHelper.wrapAndSanitize(EndNBT.INSTANCE, INBT.class, false);
        Assertions.assertEquals(StringNBT.valueOf(EndNBT.INSTANCE.toString()), sanitized);
    }

    @Test
    @DisplayName("Test serializing and deserializing EndNBT with EndNBT target and type hints")
    void testEndWithHint() {
        CCArgumentWrapperTestHelper.assertSame(EndNBT.INSTANCE, null, true);
    }

    @Test
    @DisplayName("Test serializing and deserializing EndNBT with INBT target and type hints")
    void testEndINBTWithHint() {
        CCArgumentWrapperTestHelper.assertSame(EndNBT.INSTANCE, INBT.class, true);
    }

    // ===================
    // CompoundNBT
    // ===================
    @Test
    @DisplayName("Test serializing and deserializing empty Compounds with CompoundNBT target and no hints")
    void testEmptyCompound() {
        CCArgumentWrapperTestHelper.assertSame(new CompoundNBT(), null, false);
    }

    @Test
    @DisplayName("Test serializing and deserializing empty Compounds with INBT target and no hints")
    void testEmptyCompoundINBT() {
        CCArgumentWrapperTestHelper.assertSame(new CompoundNBT(), INBT.class, false);
    }

    @Test
    @DisplayName("Test serializing and deserializing empty Compounds with CompoundNBT target and type hints")
    void testEmptyCompoundWithHint() {
        CCArgumentWrapperTestHelper.assertSame(new CompoundNBT(), null, true);
    }

    @Test
    @DisplayName("Test serializing and deserializing empty Compounds with INBT target and type hints")
    void testEmptyCompoundINBTWithHint() {
        CCArgumentWrapperTestHelper.assertSame(new CompoundNBT(), INBT.class, true);
    }

    @Test
    @DisplayName("Test serializing and deserializing simple Compounds with CompoundNBT target and no hints")
    void testCompound() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("test", "value");
        CCArgumentWrapperTestHelper.assertSame(nbt, null, false);
    }

    @Test
    @DisplayName("Test serializing and deserializing simple Compounds with INBT target and no hints")
    void testCompoundINBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("test", "value");
        CCArgumentWrapperTestHelper.assertSame(nbt, INBT.class, false);
    }

    @Test
    @DisplayName("Test serializing and deserializing simple Compounds with CompoundNBT target and type hints")
    void testCompoundWithHint() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("test", "value");
        CCArgumentWrapperTestHelper.assertSame(nbt, null, true);
    }

    @Test
    @DisplayName("Test serializing and deserializing simple Compounds with INBT target and type hints")
    void testCompoundINBTWithHint() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("test", "value");
        CCArgumentWrapperTestHelper.assertSame(nbt, INBT.class, true);
    }

    @Test
    @DisplayName("Test serializing and deserializing Compound with a key corresponding to the type hint with CompoundNBT target and no hints")
    void testCompoundWithTypeHintAsKey() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("test", "value");
        nbt.putString(CCArgumentWrapper.TYPE_HINT_KEY, "fakeHint");
        CCArgumentWrapperTestHelper.assertSame(nbt, null, false);
    }

    // ===================
    // Lists (the majority of list testing is done via property tests)
    // ===================
    @Test
    @DisplayName("Test serializing and deserializing empty lists with ListNBT target and no hints")
    void testEmptyList() {
        CCArgumentWrapperTestHelper.assertSame(new ListNBT(), null, false);
    }

    @Test
    @DisplayName("Test serializing and deserializing empty lists with INBT target and no hints")
    void testEmptyListINBT() {
        Object sanitized = CCArgumentWrapperTestHelper.wrapAndSanitize(new ListNBT(), INBT.class, false);
        Assertions.assertEquals(new CompoundNBT(), sanitized);
    }

    @Test
    @DisplayName("Test serializing and deserializing empty lists with CollectionNBT target and no hints")
    void testEmptyListCollectionNBT() {
        CCArgumentWrapperTestHelper.assertSame(new ListNBT(), CollectionNBT.class, false);
    }

    @Test
    @DisplayName("Test serializing and deserializing empty lists with ListNBT target and type hints")
    void testEmptyListWithHint() {
        CCArgumentWrapperTestHelper.assertSame(new ListNBT(), null, true);
    }

    @Test
    @DisplayName("Test serializing and deserializing empty lists with INBT target and type hints")
    void testEmptyListINBTWithHint() {
        CCArgumentWrapperTestHelper.assertSame(new ListNBT(), INBT.class, true);
    }

    @Test
    @DisplayName("Test serializing and deserializing empty lists with CollectionNBT target and type hints")
    void testEmptyListCollectionNBTWithHint() {
        CCArgumentWrapperTestHelper.assertSame(new ListNBT(), CollectionNBT.class, true);
    }

    // ===================
    // Invalid Hints
    // ===================
    private Object addInvalidHint(INBT nbt, int type) {
        //Validate that we aren't accidentally providing a valid hint
        Assertions.assertNotEquals(nbt.getId(), type);
        Map<Object, Object> hint = new HashMap<>(2);
        hint.put(CCArgumentWrapper.TYPE_HINT_KEY, (double) type);
        hint.put(CCArgumentWrapper.TYPE_HINT_VALUE_KEY, CCArgumentWrapperTestHelper.wrapNBT(nbt, false));
        return hint;
    }

    @Test
    @DisplayName("Test hint mismatch expected type using numbers as actual")
    void testInvalidHintNumber() {
        IntNBT nbt = IntNBT.valueOf(100);
        Object withHint = addInvalidHint(nbt, Constants.NBT.TAG_COMPOUND);
        Assertions.assertNotEquals(nbt, CCArgumentWrapperTestHelper.sanitize(nbt.getClass(), withHint));
    }

    @Test
    @DisplayName("Test hint mismatch expected type using compounds as actual")
    void testInvalidHintCompound() {
        CompoundNBT nbt = new CompoundNBT();
        Object withHint = addInvalidHint(nbt, Constants.NBT.TAG_INT_ARRAY);
        Assertions.assertNotEquals(nbt, CCArgumentWrapperTestHelper.sanitize(nbt.getClass(), withHint));
    }

    @Test
    @DisplayName("Test hint mismatch expected type using arrays as actual")
    void testInvalidHintCollection() {
        ByteArrayNBT nbt = new ByteArrayNBT(new byte[0]);
        Object withHint = addInvalidHint(nbt, Constants.NBT.TAG_FLOAT);
        Assertions.assertNotEquals(nbt, CCArgumentWrapperTestHelper.sanitize(nbt.getClass(), withHint));
    }

    // ===================
    // Invalid Lists
    // ===================
    private ListNBT createStringList(String... strings) {
        return Arrays.stream(strings).map(StringNBT::valueOf).collect(Collectors.toCollection(ListNBT::new));
    }

    private void assertMismatchedList(INBT... mismatchedElements) {
        //Ensure we have some elements in the list
        Assertions.assertTrue(mismatchedElements.length > 1);
        Map<Double, Object> wrappedCollection = new HashMap<>(mismatchedElements.length);
        for (int i = 0; i < mismatchedElements.length; i++) {
            wrappedCollection.put((double) i, CCArgumentWrapperTestHelper.wrapNBT(mismatchedElements[i], false));
        }
        Assertions.assertFalse(CCArgumentWrapperTestHelper.sanitize(ListNBT.class, wrappedCollection) instanceof ListNBT);
    }

    @Test
    @DisplayName("Test making sure we fail to create a list out of invalid elements (number + string)")
    void testInvalidListNumberString() {
        assertMismatchedList(IntNBT.valueOf(100), StringNBT.valueOf("Test"));
    }

    @Test
    @DisplayName("Test making sure we fail to create a list out of invalid elements (string + number)")
    void testInvalidListStringNumber() {
        assertMismatchedList(StringNBT.valueOf("Test"), IntNBT.valueOf(100));
    }

    @Test
    @DisplayName("Test making sure we fail to create a list out of invalid elements (string + byte array)")
    void testInvalidListStringByteArray() {
        assertMismatchedList(StringNBT.valueOf("Test"), new ByteArrayNBT(Collections.singletonList((byte) 0)));
    }

    @Test
    @DisplayName("Test making sure we fail to create a list out of invalid elements (string + int array)")
    void testInvalidListStringIntArray() {
        assertMismatchedList(StringNBT.valueOf("Test"), new IntArrayNBT(Collections.singletonList(Integer.MAX_VALUE)));
    }

    @Test
    @DisplayName("Test making sure we fail to create a list out of invalid elements (string + long array)")
    void testInvalidListStringLongArray() {
        assertMismatchedList(StringNBT.valueOf("Test"), new LongArrayNBT(Collections.singletonList(Long.MAX_VALUE)));
    }

    @Test
    @DisplayName("Test making sure we fail to create a list out of invalid elements (non empty compound + byte array)")
    void testInvalidListCompoundByteArray() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("key", "value");
        assertMismatchedList(nbt, new ByteArrayNBT(Collections.singletonList((byte) 0)));
    }

    @Test
    @DisplayName("Test making sure we fail to create a list out of invalid elements (non empty compound + int array)")
    void testInvalidListCompoundIntArray() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("key", "value");
        assertMismatchedList(nbt, new IntArrayNBT(Collections.singletonList(Integer.MAX_VALUE)));
    }

    @Test
    @DisplayName("Test making sure we fail to create a list out of invalid elements (non empty compound + long array)")
    void testInvalidListCompoundLongArray() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("key", "value");
        assertMismatchedList(nbt, new LongArrayNBT(Collections.singletonList(Long.MAX_VALUE)));
    }

    @Test
    @DisplayName("Test making sure we fail to create a list out of invalid elements (non empty compound + arbitrary list type (strings))")
    void testInvalidListCompoundStringList() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("key", "value");
        assertMismatchedList(nbt, createStringList("A", "B", "C"));
    }

    @Test
    @DisplayName("Test making sure we fail to create a list out of invalid elements (string + arbitrary list type (strings))")
    void testInvalidListStringStringList() {
        assertMismatchedList(StringNBT.valueOf("Test"), createStringList("A", "B", "C"));
    }
}