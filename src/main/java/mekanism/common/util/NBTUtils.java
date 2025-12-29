package mekanism.common.util;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntFunction;
import java.util.function.LongConsumer;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

@ParametersAreNotNullByDefault
public class NBTUtils {

    private NBTUtils() {
    }

    public static int[] writeBlockPositions(Collection<BlockPos> positions) {
        int[] list = new int[3 * positions.size()];
        int i = 0;
        for (BlockPos pos : positions) {
            list[i++] = pos.getX();
            list[i++] = pos.getY();
            list[i++] = pos.getZ();
        }
        return list;
    }

    public static void readBlockPositions(CompoundTag nbt, String key, Collection<BlockPos> positions) {
        if (nbt.contains(key, Tag.TAG_INT_ARRAY)) {
            int[] list = nbt.getIntArray(key);
            if (list.length % 3 == 0) {
                for (int i = 0; i < list.length;) {
                    positions.add(new BlockPos(list[i++], list[i++], list[i++]));
                }
            }
        }
    }

    public static void setBooleanIfPresent(CompoundTag nbt, String key, BooleanConsumer setter) {
        if (nbt.contains(key, Tag.TAG_BYTE)) {
            setter.accept(nbt.getBoolean(key));
        }
    }

    public static void setLongIfPresent(CompoundTag nbt, String key, LongConsumer setter) {
        if (nbt.contains(key, Tag.TAG_LONG)) {
            setter.accept(nbt.getLong(key));
        }
    }

    public static void setFloatIfPresent(CompoundTag nbt, String key, FloatConsumer setter) {
        if (nbt.contains(key, Tag.TAG_FLOAT)) {
            setter.accept(nbt.getFloat(key));
        }
    }

    public static void setDoubleIfPresent(CompoundTag nbt, String key, DoubleConsumer setter) {
        if (nbt.contains(key, Tag.TAG_DOUBLE)) {
            setter.accept(nbt.getDouble(key));
        }
    }

    public static void setListIfPresent(CompoundTag nbt, String key, int type, Consumer<ListTag> setter) {
        if (nbt.contains(key, Tag.TAG_LIST)) {
            setter.accept(nbt.getList(key, type));
        }
    }

    public static void setUUIDIfPresent(CompoundTag nbt, String key, Consumer<UUID> setter) {
        if (nbt.hasUUID(key)) {
            setter.accept(nbt.getUUID(key));
        }
    }

    public static void setFluidStackIfPresent(HolderLookup.Provider provider, CompoundTag nbt, String key, Consumer<FluidStack> setter) {
        if (nbt.contains(key, Tag.TAG_COMPOUND)) {
            setter.accept(FluidStack.parseOptional(provider, nbt.getCompound(key)));
        }
    }

    public static void setChemicalStackIfPresent(HolderLookup.Provider provider, CompoundTag nbt, String key, Consumer<ChemicalStack> setter) {
        if (nbt.contains(key, Tag.TAG_COMPOUND)) {
            setter.accept(ChemicalStack.parseOptional(provider, nbt.getCompound(key)));
        }
    }

    public static void setItemStackOrEmpty(HolderLookup.Provider provider, CompoundTag nbt, String key, Consumer<ItemStack> setter) {
        if (nbt.contains(key, Tag.TAG_COMPOUND)) {
            setter.accept(ItemStack.parseOptional(provider, nbt.getCompound(key)));
        } else {
            setter.accept(ItemStack.EMPTY);
        }
    }

    @Nullable
    public static <ENUM extends Enum<ENUM>> ENUM getEnum(CompoundTag nbt, String key, IntFunction<ENUM> indexLookup) {
        if (nbt.contains(key, Tag.TAG_INT)) {
            return indexLookup.apply(nbt.getInt(key));
        }
        return null;
    }

    public static <ENUM extends Enum<ENUM>> void setEnumIfPresent(CompoundTag nbt, String key, IntFunction<ENUM> indexLookup, Consumer<ENUM> setter) {
        if (nbt.contains(key, Tag.TAG_INT)) {
            setter.accept(indexLookup.apply(nbt.getInt(key)));
        }
    }

    public static void writeEnum(CompoundTag nbt, String key, Enum<?> e) {
        nbt.putInt(key, e.ordinal());
    }

    public static <V> void writeRegistryEntry(CompoundTag nbt, String key, Registry<V> registry, V entry) {
        Identifier registryName = registry.getKeyOrNull(entry);
        if (registryName != null) {//We expect the registry to have the entry, but if it doesn't then don't add it
            nbt.putString(key, registryName.toString());
        }
    }
}