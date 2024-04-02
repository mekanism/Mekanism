package mekanism.common.util;

import java.util.Optional;
import java.util.function.BiFunction;
import mekanism.api.NBTConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

@Deprecated//TODO - 1.21??: Remove this
public final class ItemDataUtils {

    private ItemDataUtils() {
    }

    public static <T> Optional<T> getAndRemoveData(ItemStack stack, String key, BiFunction<CompoundTag, String, T> getter) {
        CompoundTag mekData = stack.getTagElement(NBTConstants.MEK_DATA);
        if (mekData != null && mekData.contains(key)) {
            T value = getter.apply(mekData, key);
            mekData.remove(key);
            if (mekData.isEmpty()) {
                //If our data map no longer has any elements after removing a piece of stored data
                // then remove the data tag to make the stack nice and clean again
                stack.removeTagKey(NBTConstants.MEK_DATA);
            }
            return Optional.of(value);
        }
        return Optional.empty();
    }
}