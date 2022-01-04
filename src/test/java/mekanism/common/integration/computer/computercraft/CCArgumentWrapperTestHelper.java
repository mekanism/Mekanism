package mekanism.common.integration.computer.computercraft;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.Assertions;

class CCArgumentWrapperTestHelper {

    //We just pass null as the arguments as we want to test converting to given types,
    // so we can just ignore it even though it shouldn't be null, because we don't
    // call any methods that make use of it
    private static final CCArgumentWrapper DUMMY_WRAPPER = new CCArgumentWrapper(null);

    static void assertSame(Tag nbt, @Nullable Class<? extends Tag> targetClass, boolean includeHints) {
        Assertions.assertEquals(nbt, wrapAndSanitize(nbt, targetClass, includeHints));
    }

    static boolean checkSame(Tag nbt, @Nullable Class<? extends Tag> targetClass, boolean includeHints) {
        return nbt.equals(wrapAndSanitize(nbt, targetClass, includeHints));
    }

    static Object wrapAndSanitize(Tag nbt, @Nullable Class<? extends Tag> targetClass, boolean includeHints) {
        Object wrapped = wrapTag(nbt, includeHints);
        Assertions.assertNotNull(wrapped);
        return sanitize(targetClass == null ? nbt.getClass() : targetClass, wrapped);
    }

    static Object sanitize(Class<? extends Tag> expectedType, Object wrapped) {
        return DUMMY_WRAPPER.sanitizeArgument(expectedType, wrapped.getClass(), wrapped);
    }

    /**
     * Basically a copy of {@code CCArgumentWrapper#wrapTag} except slightly modified to do the conversion that would happen passing it through lua of converting all
     * numbers to doubles, and also added support for including Tag hints.
     */
    @Nullable
    static Object wrapTag(@Nullable Tag nbt, boolean includeHints) {
        if (nbt == null) {
            return null;
        }
        byte id = nbt.getId();
        switch (id) {
            case Tag.TAG_BYTE:
            case Tag.TAG_SHORT:
            case Tag.TAG_INT:
            case Tag.TAG_LONG:
            case Tag.TAG_FLOAT:
            case Tag.TAG_DOUBLE:
            case Tag.TAG_ANY_NUMERIC:
                //Get it as a double instead of a number as we will only get it back as a double from CC
                return addTagHint(id, ((NumericTag) nbt).getAsDouble(), includeHints);
            case Tag.TAG_STRING:
            case Tag.TAG_END://Tag End is highly unlikely to ever be used outside of networking but handle it anyway
                return addTagHint(id, nbt.getAsString(), includeHints);
            case Tag.TAG_BYTE_ARRAY:
            case Tag.TAG_INT_ARRAY:
            case Tag.TAG_LONG_ARRAY:
            case Tag.TAG_LIST:
                CollectionTag<?> collectionTag = (CollectionTag<?>) nbt;
                int size = collectionTag.size();
                Map<Double, Object> wrappedCollection = new HashMap<>(size);
                for (int i = 0; i < size; i++) {
                    wrappedCollection.put((double) i, wrapTag(collectionTag.get(i), includeHints));
                }
                return addTagHint(id, wrappedCollection, includeHints);
            case Tag.TAG_COMPOUND:
                CompoundTag compound = (CompoundTag) nbt;
                Map<String, Object> wrappedCompound = new HashMap<>(compound.size());
                for (String key : compound.getAllKeys()) {
                    Object value = wrapTag(compound.get(key), includeHints);
                    if (value != null) {
                        wrappedCompound.put(key, value);
                    }
                }
                return addTagHint(id, wrappedCompound, includeHints);
        }
        return null;
    }

    private static Object addTagHint(byte id, Object nbtRepresentation, boolean includeHint) {
        if (includeHint) {
            Map<Object, Object> hint = new HashMap<>(2);
            hint.put(CCArgumentWrapper.TYPE_HINT_KEY, (double) id);
            hint.put(CCArgumentWrapper.TYPE_HINT_VALUE_KEY, nbtRepresentation);
            return hint;
        }
        return nbtRepresentation;
    }
}