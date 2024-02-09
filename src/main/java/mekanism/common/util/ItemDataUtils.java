package mekanism.common.util;

import java.util.Optional;
import java.util.function.BiFunction;
import mekanism.api.NBTConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Deprecated//TODO - 1.20.4: Remove the need for as much of this as possible by rewriting usages of it into attachments
public final class ItemDataUtils {

    private ItemDataUtils() {
    }

    @NotNull
    public static CompoundTag getDataMap(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains(NBTConstants.MEK_DATA, Tag.TAG_COMPOUND)) {
            return tag.getCompound(NBTConstants.MEK_DATA);
        }
        CompoundTag dataMap = new CompoundTag();
        tag.put(NBTConstants.MEK_DATA, dataMap);
        return dataMap;
    }

    @Nullable
    public static CompoundTag getDataMapIfPresent(ItemStack stack) {
        return getMekData(stack).orElse(null);
    }


    public static Optional<CompoundTag> getMekData(ItemStack stack) {
        return Optional.ofNullable(stack.getTag())
              .filter(tag -> tag.contains(NBTConstants.MEK_DATA, Tag.TAG_COMPOUND))
              .map(tag -> tag.getCompound(NBTConstants.MEK_DATA));
    }

    public static <T> Optional<T> getAndRemoveData(ItemStack stack, String key, BiFunction<CompoundTag, String, T> getter) {
        return getMekData(stack)
              .filter(mekData -> mekData.contains(key))
              .map(mekData -> {
                  T value = getter.apply(mekData, key);
                  mekData.remove(key);
                  if (mekData.isEmpty()) {
                      //If our data map no longer has any elements after removing a piece of stored data
                      // then remove the data tag to make the stack nice and clean again
                      stack.removeTagKey(NBTConstants.MEK_DATA);
                  }
                  return value;
              });
    }

    public static void setCompound(ItemStack stack, String key, CompoundTag tag) {
        getDataMap(stack).put(key, tag);
    }
}