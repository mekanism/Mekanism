package mekanism.common.util;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

//TODO - V11: Rewrite this into a helper object that gets made for a stack so that we can easier make sure it doesn't add any extra data when we don't want it
// And then for some things we may want when they go back to a full empty state make the NBT go away
public final class ItemDataUtils {

    private ItemDataUtils() {
    }

    @Nonnull
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
                CompoundTag tag = stack.getTag();
                tag.remove(NBTConstants.MEK_DATA);
                if (tag.isEmpty()) {
                    //If the tag is fully empty now just remove it entirely
                    stack.setTag(null);
                }
            }
        }
    }

    public static <T> T getDataValue(ItemStack stack, Function<CompoundTag, T> getter, T fallback) {
        CompoundTag dataMap = getDataMapIfPresent(stack);
        return dataMap == null ? fallback : getter.apply(dataMap);
    }

    public static int getInt(ItemStack stack, String key) {
        CompoundTag dataMap = getDataMapIfPresent(stack);
        return dataMap == null ? 0 : dataMap.getInt(key);
    }

    public static long getLong(ItemStack stack, String key) {
        CompoundTag dataMap = getDataMapIfPresent(stack);
        return dataMap == null ? 0 : dataMap.getLong(key);
    }

    public static boolean getBoolean(ItemStack stack, String key) {
        CompoundTag dataMap = getDataMapIfPresent(stack);
        return dataMap != null && dataMap.getBoolean(key);
    }

    public static double getDouble(ItemStack stack, String key) {
        CompoundTag dataMap = getDataMapIfPresent(stack);
        return dataMap == null ? 0 : dataMap.getDouble(key);
    }

    public static String getString(ItemStack stack, String key) {
        return getDataValue(stack, dataMap -> dataMap.getString(key), "");
    }

    public static CompoundTag getCompound(ItemStack stack, String key) {
        return getDataValue(stack, dataMap -> dataMap.getCompound(key), new CompoundTag());
    }

    public static CompoundTag getOrAddCompound(ItemStack stack, String key) {
        CompoundTag dataMap = getDataMap(stack);
        if (dataMap.contains(key, Tag.TAG_COMPOUND)) {
            return dataMap.getCompound(key);
        }
        CompoundTag compound = new CompoundTag();
        dataMap.put(key, compound);
        return compound;
    }

    public static void setCompoundIfPresent(ItemStack stack, String key, Consumer<CompoundTag> setter) {
        CompoundTag dataMap = getDataMapIfPresent(stack);
        if (dataMap != null && dataMap.contains(key, Tag.TAG_COMPOUND)) {
            setter.accept(dataMap.getCompound(key));
        }
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

    public static void setInt(ItemStack stack, String key, int i) {
        getDataMap(stack).putInt(key, i);
    }

    public static void setIntOrRemove(ItemStack stack, String key, int i) {
        if (i == 0) {
            removeData(stack, key);
        } else {
            setInt(stack, key, i);
        }
    }

    public static void setLong(ItemStack stack, String key, long l) {
        getDataMap(stack).putLong(key, l);
    }

    public static void setLongOrRemove(ItemStack stack, String key, long l) {
        if (l == 0) {
            removeData(stack, key);
        } else {
            setLong(stack, key, l);
        }
    }

    public static void setBoolean(ItemStack stack, String key, boolean b) {
        getDataMap(stack).putBoolean(key, b);
    }

    public static void setDouble(ItemStack stack, String key, double d) {
        getDataMap(stack).putDouble(key, d);
    }

    public static void setString(ItemStack stack, String key, String s) {
        getDataMap(stack).putString(key, s);
    }

    public static void setCompound(ItemStack stack, String key, CompoundTag tag) {
        getDataMap(stack).put(key, tag);
    }

    public static void setUUID(ItemStack stack, String key, @Nullable UUID uuid) {
        if (uuid == null) {
            removeData(stack, key);
        } else {
            getDataMap(stack).putUUID(key, uuid);
        }
    }

    public static void setList(ItemStack stack, String key, ListTag tag) {
        getDataMap(stack).put(key, tag);
    }

    public static void setListOrRemove(ItemStack stack, String key, ListTag tag) {
        if (tag.isEmpty()) {
            removeData(stack, key);
        } else {
            setList(stack, key, tag);
        }
    }

    public static void readContainers(ItemStack stack, String containerKey, List<? extends INBTSerializable<CompoundTag>> containers) {
        if (!stack.isEmpty()) {
            DataHandlerUtils.readContainers(containers, getList(stack, containerKey));
        }
    }

    public static void writeContainers(ItemStack stack, String containerKey, List<? extends INBTSerializable<CompoundTag>> containers) {
        if (!stack.isEmpty()) {
            setListOrRemove(stack, containerKey, DataHandlerUtils.writeContainers(containers));
        }
    }
}