package mekanism.common.util;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import mekanism.api.NBTConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(NBTConstants.MEK_DATA, Tag.TAG_COMPOUND)) {
            return tag.getCompound(NBTConstants.MEK_DATA);
        }
        return null;
    }

    public static boolean hasData(ItemStack stack, String key, int type) {
        CompoundTag dataMap = getDataMapIfPresent(stack);
        return dataMap != null && dataMap.contains(key, type);
    }

    public static void removeData(ItemStack stack, String key) {
        CompoundTag dataMap = getDataMapIfPresent(stack);
        if (dataMap != null) {
            dataMap.remove(key);
            if (dataMap.isEmpty()) {
                //If our data map no longer has any elements after removing a piece of stored data
                // then remove the data tag to make the stack nice and clean again
                stack.removeTagKey(NBTConstants.MEK_DATA);
            }
        }
    }

    private static <T> T getDataValue(ItemStack stack, Function<CompoundTag, T> getter, T fallback) {
        CompoundTag dataMap = getDataMapIfPresent(stack);
        return dataMap == null ? fallback : getter.apply(dataMap);
    }

    public static <T> Optional<T> getAndRemoveData(ItemStack stack, String key, BiFunction<CompoundTag, String, T> getter) {
        return Optional.ofNullable(stack.getTag())
              .filter(tag -> tag.contains(NBTConstants.MEK_DATA, Tag.TAG_COMPOUND))
              .map(tag -> tag.getCompound(NBTConstants.MEK_DATA))
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

    public static int getInt(ItemStack stack, String key) {
        CompoundTag dataMap = getDataMapIfPresent(stack);
        return dataMap == null ? 0 : dataMap.getInt(key);
    }

    public static CompoundTag getCompound(ItemStack stack, String key) {
        return getDataValue(stack, dataMap -> dataMap.getCompound(key), new CompoundTag());
    }

    @Nullable
    public static UUID getUniqueID(ItemStack stack, String key) {
        CompoundTag dataMap = getDataMapIfPresent(stack);
        if (dataMap != null && dataMap.hasUUID(key)) {
            return dataMap.getUUID(key);
        }
        return null;
    }

    public static ListTag getList(ItemStack stack, String key) {
        return getDataValue(stack, dataMap -> dataMap.getList(key, Tag.TAG_COMPOUND), new ListTag());
    }

    public static void setCompound(ItemStack stack, String key, CompoundTag tag) {
        getDataMap(stack).put(key, tag);
    }

    public static void setListOrRemove(ItemStack stack, String key, ListTag tag) {
        if (tag.isEmpty()) {
            removeData(stack, key);
        } else {
            getDataMap(stack).put(key, tag);
        }
    }
}