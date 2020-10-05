package mekanism.common.util;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants.NBT;

//TODO - V11: Rewrite this into a helper object that gets made for a stack so that we can easier make sure it doesn't add any extra data when we don't want it
// And then for some things we may want when they go back to a full empty state make the NBT go away
public final class ItemDataUtils {

    private ItemDataUtils() {
    }

    @Nonnull
    public static CompoundNBT getDataMap(ItemStack stack) {
        initStack(stack);
        return stack.getTag().getCompound(NBTConstants.MEK_DATA);
    }

    @Nullable
    public static CompoundNBT getDataMapIfPresent(ItemStack stack) {
        return hasDataTag(stack) ? getDataMap(stack) : null;
    }

    @Nonnull
    public static CompoundNBT getDataMapIfPresentNN(ItemStack stack) {
        return hasDataTag(stack) ? getDataMap(stack) : new CompoundNBT();
    }

    public static boolean hasData(ItemStack stack, String key, int type) {
        return hasDataTag(stack) && getDataMap(stack).contains(key, type);
    }

    public static boolean hasUUID(ItemStack stack, String key) {
        return hasDataTag(stack) && getDataMap(stack).hasUniqueId(key);
    }

    public static void removeData(ItemStack stack, String key) {
        if (hasDataTag(stack)) {
            CompoundNBT dataMap = getDataMap(stack);
            dataMap.remove(key);
            if (dataMap.isEmpty()) {
                //If our data map no longer has any elements after removing a piece of stored data
                // then remove the data tag to make the stack nice and clean again
                stack.getTag().remove(NBTConstants.MEK_DATA);
            }
        }
    }

    public static int getInt(ItemStack stack, String key) {
        return hasDataTag(stack) ? getDataMap(stack).getInt(key) : 0;
    }

    public static long getLong(ItemStack stack, String key) {
        return hasDataTag(stack) ? getDataMap(stack).getLong(key) : 0;
    }

    public static boolean getBoolean(ItemStack stack, String key) {
        return hasDataTag(stack) && getDataMap(stack).getBoolean(key);
    }

    public static double getDouble(ItemStack stack, String key) {
        return hasDataTag(stack) ? getDataMap(stack).getDouble(key) : 0;
    }

    public static String getString(ItemStack stack, String key) {
        return hasDataTag(stack) ? getDataMap(stack).getString(key) : "";
    }

    public static CompoundNBT getCompound(ItemStack stack, String key) {
        return hasDataTag(stack) ? getDataMap(stack).getCompound(key) : new CompoundNBT();
    }

    @Nullable
    public static UUID getUniqueID(ItemStack stack, String key) {
        return hasDataTag(stack) ? getDataMap(stack).getUniqueId(key) : null;
    }

    public static ListNBT getList(ItemStack stack, String key) {
        return hasDataTag(stack) ? getDataMap(stack).getList(key, NBT.TAG_COMPOUND) : new ListNBT();
    }

    public static void setInt(ItemStack stack, String key, int i) {
        initStack(stack);
        getDataMap(stack).putInt(key, i);
    }

    public static void setLong(ItemStack stack, String key, long l) {
        initStack(stack);
        getDataMap(stack).putLong(key, l);
    }

    public static void setBoolean(ItemStack stack, String key, boolean b) {
        initStack(stack);
        getDataMap(stack).putBoolean(key, b);
    }

    public static void setDouble(ItemStack stack, String key, double d) {
        initStack(stack);
        getDataMap(stack).putDouble(key, d);
    }

    public static void setString(ItemStack stack, String key, String s) {
        initStack(stack);
        getDataMap(stack).putString(key, s);
    }

    public static void setCompound(ItemStack stack, String key, CompoundNBT tag) {
        initStack(stack);
        getDataMap(stack).put(key, tag);
    }

    public static void setUUID(ItemStack stack, String key, UUID uuid) {
        initStack(stack);
        getDataMap(stack).putUniqueId(key, uuid);
    }

    public static void setList(ItemStack stack, String key, ListNBT tag) {
        initStack(stack);
        getDataMap(stack).put(key, tag);
    }

    private static boolean hasDataTag(ItemStack stack) {
        return stack.getTag() != null && stack.getTag().contains(NBTConstants.MEK_DATA, NBT.TAG_COMPOUND);
    }

    private static void initStack(ItemStack stack) {
        CompoundNBT tag = stack.getOrCreateTag();
        if (!tag.contains(NBTConstants.MEK_DATA, NBT.TAG_COMPOUND)) {
            tag.put(NBTConstants.MEK_DATA, new CompoundNBT());
        }
    }
}