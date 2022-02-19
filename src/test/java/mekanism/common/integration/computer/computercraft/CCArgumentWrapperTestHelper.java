package mekanism.common.integration.computer.computercraft;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.nbt.CollectionNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NumberNBT;
import net.minecraftforge.common.util.Constants;
import org.junit.jupiter.api.Assertions;

class CCArgumentWrapperTestHelper {

    //We just pass null as the arguments as we want to test converting to given types,
    // so we can just ignore it even though it shouldn't be null, because we don't
    // call any methods that make use of it
    private static final CCArgumentWrapper DUMMY_WRAPPER = new CCArgumentWrapper(null);

    static void assertSame(INBT nbt, @Nullable Class<? extends INBT> targetClass, boolean includeHints) {
        Assertions.assertEquals(nbt, wrapAndSanitize(nbt, targetClass, includeHints));
    }

    static boolean checkSame(INBT nbt, @Nullable Class<? extends INBT> targetClass, boolean includeHints) {
        return nbt.equals(wrapAndSanitize(nbt, targetClass, includeHints));
    }

    static Object wrapAndSanitize(INBT nbt, @Nullable Class<? extends INBT> targetClass, boolean includeHints) {
        Object wrapped = wrapNBT(nbt, includeHints);
        Assertions.assertNotNull(wrapped);
        return sanitize(targetClass == null ? nbt.getClass() : targetClass, wrapped);
    }

    static Object sanitize(Class<? extends INBT> expectedType, Object wrapped) {
        return DUMMY_WRAPPER.sanitizeArgument(expectedType, wrapped.getClass(), wrapped);
    }

    /**
     * Basically a copy of {@code CCArgumentWrapper#wrapNBT} except slightly modified to do the conversion that would happen passing it through lua of converting all
     * numbers to doubles, and also added support for including NBT hints.
     */
    @Nullable
    static Object wrapNBT(@Nullable INBT nbt, boolean includeHints) {
        if (nbt == null) {
            return null;
        }
        byte id = nbt.getId();
        switch (id) {
            case Constants.NBT.TAG_BYTE:
            case Constants.NBT.TAG_SHORT:
            case Constants.NBT.TAG_INT:
            case Constants.NBT.TAG_LONG:
            case Constants.NBT.TAG_FLOAT:
            case Constants.NBT.TAG_DOUBLE:
            case Constants.NBT.TAG_ANY_NUMERIC:
                //Get it as a double instead of a number as we will only get it back as a double from CC
                return addNBTHint(id, ((NumberNBT) nbt).getAsDouble(), includeHints);
            case Constants.NBT.TAG_STRING:
            case Constants.NBT.TAG_END://Tag End is highly unlikely to ever be used outside of networking but handle it anyway
                return addNBTHint(id, nbt.getAsString(), includeHints);
            case Constants.NBT.TAG_BYTE_ARRAY:
            case Constants.NBT.TAG_INT_ARRAY:
            case Constants.NBT.TAG_LONG_ARRAY:
            case Constants.NBT.TAG_LIST:
                CollectionNBT<?> collectionNBT = (CollectionNBT<?>) nbt;
                int size = collectionNBT.size();
                Map<Double, Object> wrappedCollection = new HashMap<>(size);
                for (int i = 0; i < size; i++) {
                    wrappedCollection.put((double) i, wrapNBT(collectionNBT.get(i), includeHints));
                }
                return addNBTHint(id, wrappedCollection, includeHints);
            case Constants.NBT.TAG_COMPOUND:
                CompoundNBT compound = (CompoundNBT) nbt;
                Map<String, Object> wrappedCompound = new HashMap<>(compound.size());
                for (String key : compound.getAllKeys()) {
                    Object value = wrapNBT(compound.get(key), includeHints);
                    if (value != null) {
                        wrappedCompound.put(key, value);
                    }
                }
                return addNBTHint(id, wrappedCompound, includeHints);
        }
        return null;
    }

    private static Object addNBTHint(byte id, Object nbtRepresentation, boolean includeHint) {
        if (includeHint) {
            Map<Object, Object> hint = new HashMap<>(2);
            hint.put(CCArgumentWrapper.TYPE_HINT_KEY, (double) id);
            hint.put(CCArgumentWrapper.TYPE_HINT_VALUE_KEY, nbtRepresentation);
            return hint;
        }
        return nbtRepresentation;
    }
}