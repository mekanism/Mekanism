package mekanism.common.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants.NBT;

//TODO: Use ItemStack#getOrCreateTag in various places
//TODO: Move constants to some sort of constant file
//TODO: For some things like energy (example energy tablet), do we want to make the NBT go away once it is fully drained again
// this probably would be the best way to do it for mod compat with auto crafters
public final class ItemDataUtils {

    public static final String DATA_ID = "mekData";

    @Nonnull
    public static CompoundNBT getDataMap(ItemStack stack) {
        initStack(stack);
        return stack.getTag().getCompound(DATA_ID);
    }

    @Nullable
    public static CompoundNBT getDataMapIfPresent(ItemStack stack) {
        return hasDataTag(stack) ? getDataMap(stack) : null;
    }

    @Nonnull
    public static CompoundNBT getDataMapIfPresentNN(ItemStack stack) {
        return hasDataTag(stack) ? getDataMap(stack) : new CompoundNBT();
    }

    public static boolean hasData(ItemStack stack, String key) {
        if (!hasDataTag(stack)) {
            return false;
        }
        return getDataMap(stack).contains(key);
    }

    public static void removeData(ItemStack stack, String key) {
        if (!hasDataTag(stack)) {
            return;
        }
        getDataMap(stack).remove(key);
    }

    public static int getInt(ItemStack stack, String key) {
        if (!hasDataTag(stack)) {
            return 0;
        }
        return getDataMap(stack).getInt(key);
    }

    public static boolean getBoolean(ItemStack stack, String key) {
        if (!hasDataTag(stack)) {
            return false;
        }
        return getDataMap(stack).getBoolean(key);
    }

    public static double getDouble(ItemStack stack, String key) {
        if (!hasDataTag(stack)) {
            return 0;
        }
        return getDataMap(stack).getDouble(key);
    }

    public static String getString(ItemStack stack, String key) {
        if (!hasDataTag(stack)) {
            return "";
        }
        return getDataMap(stack).getString(key);
    }

    public static CompoundNBT getCompound(ItemStack stack, String key) {
        if (!hasDataTag(stack)) {
            return new CompoundNBT();
        }
        return getDataMap(stack).getCompound(key);
    }

    public static ListNBT getList(ItemStack stack, String key) {
        if (!hasDataTag(stack)) {
            return new ListNBT();
        }
        return getDataMap(stack).getList(key, NBT.TAG_COMPOUND);
    }

    public static void setInt(ItemStack stack, String key, int i) {
        initStack(stack);
        getDataMap(stack).putInt(key, i);
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

    public static void setList(ItemStack stack, String key, ListNBT tag) {
        initStack(stack);
        getDataMap(stack).put(key, tag);
    }

    private static boolean hasDataTag(ItemStack stack) {
        return stack.getTag() != null && stack.getTag().contains(DATA_ID);
    }

    private static void initStack(ItemStack stack) {
        CompoundNBT tag = stack.getOrCreateTag();
        if (!tag.contains(DATA_ID)) {
            tag.put(DATA_ID, new CompoundNBT());
        }
    }
}