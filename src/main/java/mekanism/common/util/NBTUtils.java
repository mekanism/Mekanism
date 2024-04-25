package mekanism.common.util;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.bytes.ByteConsumer;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import it.unimi.dsi.fastutil.shorts.ShortConsumer;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.LongConsumer;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.merged.BoxedChemical;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

@ParametersAreNotNullByDefault
public class NBTUtils {

    private NBTUtils() {
    }

    public static IntArrayTag writeBlockPositions(Collection<BlockPos> positions) {
        int[] list = new int[3 * positions.size()];
        int i = 0;
        for (BlockPos pos : positions) {
            list[i++] = pos.getX();
            list[i++] = pos.getY();
            list[i++] = pos.getZ();
        }
        return new IntArrayTag(list);
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

    public static void setByteIfPresent(CompoundTag nbt, String key, ByteConsumer setter) {
        if (nbt.contains(key, Tag.TAG_BYTE)) {
            setter.accept(nbt.getByte(key));
        }
    }

    public static void setBooleanIfPresent(CompoundTag nbt, String key, BooleanConsumer setter) {
        if (nbt.contains(key, Tag.TAG_BYTE)) {
            setter.accept(nbt.getBoolean(key));
        }
    }

    public static void setBooleanIfPresentElse(CompoundTag nbt, String key, boolean fallback, BooleanConsumer setter) {
        if (nbt.contains(key, Tag.TAG_BYTE)) {
            setter.accept(nbt.getBoolean(key));
        } else {
            setter.accept(fallback);
        }
    }

    public static void setShortIfPresent(CompoundTag nbt, String key, ShortConsumer setter) {
        if (nbt.contains(key, Tag.TAG_SHORT)) {
            setter.accept(nbt.getShort(key));
        }
    }

    public static void setIntIfPresent(CompoundTag nbt, String key, IntConsumer setter) {
        if (nbt.contains(key, Tag.TAG_INT)) {
            setter.accept(nbt.getInt(key));
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

    public static void setByteArrayIfPresent(CompoundTag nbt, String key, Consumer<byte[]> setter) {
        if (nbt.contains(key, Tag.TAG_BYTE_ARRAY)) {
            setter.accept(nbt.getByteArray(key));
        }
    }

    public static void setStringIfPresent(CompoundTag nbt, String key, Consumer<String> setter) {
        if (nbt.contains(key, Tag.TAG_STRING)) {
            setter.accept(nbt.getString(key));
        }
    }

    public static void setListIfPresent(CompoundTag nbt, String key, int type, Consumer<ListTag> setter) {
        if (nbt.contains(key, Tag.TAG_LIST)) {
            setter.accept(nbt.getList(key, type));
        }
    }

    public static void setCompoundIfPresent(CompoundTag nbt, String key, Consumer<CompoundTag> setter) {
        if (nbt.contains(key, Tag.TAG_COMPOUND)) {
            setter.accept(nbt.getCompound(key));
        }
    }

    public static void setIntArrayIfPresent(CompoundTag nbt, String key, Consumer<int[]> setter) {
        if (nbt.contains(key, Tag.TAG_INT_ARRAY)) {
            setter.accept(nbt.getIntArray(key));
        }
    }

    public static void setLongArrayIfPresent(CompoundTag nbt, String key, Consumer<long[]> setter) {
        if (nbt.contains(key, Tag.TAG_LONG_ARRAY)) {
            setter.accept(nbt.getLongArray(key));
        }
    }

    public static void setUUIDIfPresent(CompoundTag nbt, String key, Consumer<UUID> setter) {
        if (nbt.hasUUID(key)) {
            setter.accept(nbt.getUUID(key));
        }
    }

    public static void setUUIDIfPresentElse(CompoundTag nbt, String key, Consumer<UUID> setter, Runnable notPresent) {
        if (nbt.hasUUID(key)) {
            setter.accept(nbt.getUUID(key));
        } else {
            notPresent.run();
        }
    }

    public static void setBlockPosIfPresent(CompoundTag nbt, String key, Consumer<BlockPos> setter) {
        NbtUtils.readBlockPos(nbt, key).ifPresent(setter);
    }

    public static void setFluidStackIfPresent(HolderLookup.Provider provider, CompoundTag nbt, String key, Consumer<FluidStack> setter) {
        if (nbt.contains(key, Tag.TAG_COMPOUND)) {
            setter.accept(FluidStack.parseOptional(provider, nbt.getCompound(key)));
        }
    }

    public static void setBoxedChemicalIfPresent(CompoundTag nbt, String key, Consumer<BoxedChemical> setter) {
        if (nbt.contains(key, Tag.TAG_COMPOUND)) {
            setter.accept(BoxedChemical.read(nbt.getCompound(key)));
        }
    }

    public static void setGasIfPresent(CompoundTag nbt, String key, Consumer<Gas> setter) {
        if (nbt.contains(key, Tag.TAG_COMPOUND)) {
            setter.accept(Gas.readFromNBT(nbt.getCompound(key)));
        }
    }

    public static void setGasStackIfPresent(CompoundTag nbt, String key, Consumer<GasStack> setter) {
        if (nbt.contains(key, Tag.TAG_COMPOUND)) {
            setter.accept(GasStack.readFromNBT(nbt.getCompound(key)));
        }
    }

    public static void setInfuseTypeIfPresent(CompoundTag nbt, String key, Consumer<InfuseType> setter) {
        if (nbt.contains(key, Tag.TAG_COMPOUND)) {
            setter.accept(InfuseType.readFromNBT(nbt.getCompound(key)));
        }
    }

    public static void setInfusionStackIfPresent(CompoundTag nbt, String key, Consumer<InfusionStack> setter) {
        if (nbt.contains(key, Tag.TAG_COMPOUND)) {
            setter.accept(InfusionStack.readFromNBT(nbt.getCompound(key)));
        }
    }

    public static void setPigmentIfPresent(CompoundTag nbt, String key, Consumer<Pigment> setter) {
        if (nbt.contains(key, Tag.TAG_COMPOUND)) {
            setter.accept(Pigment.readFromNBT(nbt.getCompound(key)));
        }
    }

    public static void setPigmentStackIfPresent(CompoundTag nbt, String key, Consumer<PigmentStack> setter) {
        if (nbt.contains(key, Tag.TAG_COMPOUND)) {
            setter.accept(PigmentStack.readFromNBT(nbt.getCompound(key)));
        }
    }

    public static void setSlurryIfPresent(CompoundTag nbt, String key, Consumer<Slurry> setter) {
        if (nbt.contains(key, Tag.TAG_COMPOUND)) {
            setter.accept(Slurry.readFromNBT(nbt.getCompound(key)));
        }
    }

    public static void setSlurryStackIfPresent(CompoundTag nbt, String key, Consumer<SlurryStack> setter) {
        if (nbt.contains(key, Tag.TAG_COMPOUND)) {
            setter.accept(SlurryStack.readFromNBT(nbt.getCompound(key)));
        }
    }

    public static void setFloatingLongIfPresent(CompoundTag nbt, String key, FloatingLongConsumer setter) {
        if (nbt.contains(key, Tag.TAG_STRING)) {
            try {
                setter.accept(FloatingLong.parseFloatingLong(nbt.getString(key)));
            } catch (NumberFormatException e) {
                setter.accept(FloatingLong.ZERO);
            }
        }
    }

    public static void setItemStackIfPresent(HolderLookup.Provider provider, CompoundTag nbt, String key, Consumer<ItemStack> setter) {
        if (nbt.contains(key, Tag.TAG_COMPOUND)) {
            setter.accept(ItemStack.parseOptional(provider, nbt.getCompound(key)));
        }
    }

    public static void setItemStackOrEmpty(HolderLookup.Provider provider, CompoundTag nbt, String key, Consumer<ItemStack> setter) {
        if (nbt.contains(key, Tag.TAG_COMPOUND)) {
            setter.accept(ItemStack.parseOptional(provider, nbt.getCompound(key)));
        } else {
            setter.accept(ItemStack.EMPTY);
        }
    }

    public static void setResourceLocationIfPresent(CompoundTag nbt, String key, Consumer<ResourceLocation> setter) {
        if (nbt.contains(key, Tag.TAG_STRING)) {
            ResourceLocation value = ResourceLocation.tryParse(nbt.getString(key));
            if (value != null) {
                setter.accept(value);
            }
        }
    }

    public static void setResourceLocationIfPresentElse(CompoundTag nbt, String key, Consumer<ResourceLocation> setter, Runnable notPresent) {
        if (nbt.contains(key, Tag.TAG_STRING)) {
            ResourceLocation value = ResourceLocation.tryParse(nbt.getString(key));
            if (value == null) {
                notPresent.run();
            } else {
                setter.accept(value);
            }
        }
    }

    public static <REG> void setRegistryEntryIfPresentElse(CompoundTag nbt, String key, Registry<REG> registry, Consumer<REG> setter, Runnable notPresent) {
        setResourceLocationIfPresentElse(nbt, key, rl -> {
            Optional<REG> reg = registry.getOptional(rl);
            if (reg.isEmpty()) {
                notPresent.run();
            } else {
                setter.accept(reg.get());
            }
        }, notPresent);
    }

    public static <REG> void setResourceKeyIfPresentElse(CompoundTag nbt, String key, ResourceKey<? extends Registry<REG>> registryName, Consumer<ResourceKey<REG>> setter,
          Runnable notPresent) {
        setResourceLocationIfPresentElse(nbt, key, rl -> setter.accept(ResourceKey.create(registryName, rl)), notPresent);
    }

    @Nullable
    public static <ENUM extends Enum<ENUM>> ENUM getEnum(CompoundTag nbt, String key, IntFunction<ENUM> indexLookup) {
        if (nbt.contains(key, Tag.TAG_INT)) {
            return indexLookup.apply(nbt.getInt(key));
        }
        return null;
    }

    //TODO - 1.20.4: Replace some usages of this with getEnum
    public static <ENUM extends Enum<ENUM>> void setEnumIfPresent(CompoundTag nbt, String key, IntFunction<ENUM> indexLookup, Consumer<ENUM> setter) {
        if (nbt.contains(key, Tag.TAG_INT)) {
            setter.accept(indexLookup.apply(nbt.getInt(key)));
        }
    }

    public static void writeEnum(CompoundTag nbt, String key, Enum<?> e) {
        nbt.putInt(key, e.ordinal());
    }

    public static <V> V readRegistryEntry(CompoundTag nbt, String key, Registry<V> registry, V fallback) {
        if (nbt.contains(key, Tag.TAG_STRING)) {
            ResourceLocation rl = ResourceLocation.tryParse(nbt.getString(key));
            if (rl != null) {
                //Bypass it falling back to the default by using getOptional instead of get
                return registry.getOptional(rl).orElse(fallback);
            }
        }
        return fallback;
    }

    public static <V> void writeRegistryEntry(CompoundTag nbt, String key, Registry<V> registry, Holder<V> entry) {
        writeRegistryEntry(nbt, key, registry, entry.value());
    }

    public static <V> void writeRegistryEntry(CompoundTag nbt, String key, Registry<V> registry, V entry) {
        ResourceLocation registryName = registry.getKey(entry);
        if (registryName != null) {//Should not be null but validate it
            nbt.putString(key, registryName.toString());
        }
    }

    public static void writeResourceKey(CompoundTag nbt, String key, ResourceKey<?> entry) {
        nbt.putString(key, entry.location().toString());
    }
}