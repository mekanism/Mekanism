package mekanism.common.util;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.bytes.ByteConsumer;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.shorts.ShortConsumer;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.Coord4D;
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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidStack;

@ParametersAreNonnullByDefault
public class NBTUtils {

    private NBTUtils() {
    }

    public static void setByteIfPresent(CompoundNBT nbt, String key, ByteConsumer setter) {
        if (nbt.contains(key, NBT.TAG_BYTE)) {
            setter.accept(nbt.getByte(key));
        }
    }

    public static void setBooleanIfPresent(CompoundNBT nbt, String key, BooleanConsumer setter) {
        if (nbt.contains(key, NBT.TAG_BYTE)) {
            setter.accept(nbt.getBoolean(key));
        }
    }

    public static void setShortIfPresent(CompoundNBT nbt, String key, ShortConsumer setter) {
        if (nbt.contains(key, NBT.TAG_SHORT)) {
            setter.accept(nbt.getShort(key));
        }
    }

    public static void setIntIfPresent(CompoundNBT nbt, String key, IntConsumer setter) {
        if (nbt.contains(key, NBT.TAG_INT)) {
            setter.accept(nbt.getInt(key));
        }
    }

    public static void setLongIfPresent(CompoundNBT nbt, String key, LongConsumer setter) {
        if (nbt.contains(key, NBT.TAG_LONG)) {
            setter.accept(nbt.getLong(key));
        }
    }

    public static void setFloatIfPresent(CompoundNBT nbt, String key, FloatConsumer setter) {
        if (nbt.contains(key, NBT.TAG_FLOAT)) {
            setter.accept(nbt.getFloat(key));
        }
    }

    public static void setDoubleIfPresent(CompoundNBT nbt, String key, DoubleConsumer setter) {
        if (nbt.contains(key, NBT.TAG_DOUBLE)) {
            setter.accept(nbt.getDouble(key));
        }
    }

    public static void setByteArrayIfPresent(CompoundNBT nbt, String key, Consumer<byte[]> setter) {
        if (nbt.contains(key, NBT.TAG_BYTE_ARRAY)) {
            setter.accept(nbt.getByteArray(key));
        }
    }

    public static void setStringIfPresent(CompoundNBT nbt, String key, Consumer<String> setter) {
        if (nbt.contains(key, NBT.TAG_STRING)) {
            setter.accept(nbt.getString(key));
        }
    }

    public static void setListIfPresent(CompoundNBT nbt, String key, int type, Consumer<ListNBT> setter) {
        if (nbt.contains(key, NBT.TAG_LIST)) {
            setter.accept(nbt.getList(key, type));
        }
    }

    public static void setCompoundIfPresent(CompoundNBT nbt, String key, Consumer<CompoundNBT> setter) {
        if (nbt.contains(key, NBT.TAG_COMPOUND)) {
            setter.accept(nbt.getCompound(key));
        }
    }

    public static void setIntArrayIfPresent(CompoundNBT nbt, String key, Consumer<int[]> setter) {
        if (nbt.contains(key, NBT.TAG_INT_ARRAY)) {
            setter.accept(nbt.getIntArray(key));
        }
    }

    public static void setLongArrayIfPresent(CompoundNBT nbt, String key, Consumer<long[]> setter) {
        if (nbt.contains(key, NBT.TAG_LONG_ARRAY)) {
            setter.accept(nbt.getLongArray(key));
        }
    }

    public static boolean hasOldUUID(CompoundNBT nbt, String key) {
        return nbt.contains(key + "Most", NBT.TAG_ANY_NUMERIC) && nbt.contains(key + "Least", NBT.TAG_ANY_NUMERIC);
    }

    public static UUID getOldUUID(CompoundNBT nbt, String key) {
        return new UUID(nbt.getLong(key + "Most"), nbt.getLong(key + "Least"));
    }

    public static void setUUIDIfPresent(CompoundNBT nbt, String key, Consumer<UUID> setter) {
        if (nbt.hasUniqueId(key)) {
            setter.accept(nbt.getUniqueId(key));
        } else if (hasOldUUID(nbt, key)) {
            setter.accept(getOldUUID(nbt, key));
        }
    }

    public static void setUUIDIfPresentElse(CompoundNBT nbt, String key, Consumer<UUID> setter, Runnable notPresent) {
        if (nbt.hasUniqueId(key)) {
            setter.accept(nbt.getUniqueId(key));
        } else if (hasOldUUID(nbt, key)) {
            setter.accept(getOldUUID(nbt, key));
        } else {
            notPresent.run();
        }
    }

    public static void setBlockPosIfPresent(CompoundNBT nbt, String key, Consumer<BlockPos> setter) {
        if (nbt.contains(key, NBT.TAG_COMPOUND)) {
            setter.accept(NBTUtil.readBlockPos(nbt.getCompound(key)));
        }
    }

    public static void setCoord4DIfPresent(CompoundNBT nbt, String key, Consumer<Coord4D> setter) {
        if (nbt.contains(key, NBT.TAG_COMPOUND)) {
            setter.accept(Coord4D.read(nbt.getCompound(key)));
        }
    }

    public static void setFluidStackIfPresent(CompoundNBT nbt, String key, Consumer<FluidStack> setter) {
        if (nbt.contains(key, NBT.TAG_COMPOUND)) {
            setter.accept(FluidStack.loadFluidStackFromNBT(nbt.getCompound(key)));
        }
    }

    public static void setBoxedChemicalIfPresent(CompoundNBT nbt, String key, Consumer<BoxedChemical> setter) {
        if (nbt.contains(key, NBT.TAG_COMPOUND)) {
            setter.accept(BoxedChemical.read(nbt.getCompound(key)));
        }
    }

    public static void setGasIfPresent(CompoundNBT nbt, String key, Consumer<Gas> setter) {
        if (nbt.contains(key, NBT.TAG_COMPOUND)) {
            setter.accept(Gas.readFromNBT(nbt.getCompound(key)));
        }
    }

    public static void setGasStackIfPresent(CompoundNBT nbt, String key, Consumer<GasStack> setter) {
        if (nbt.contains(key, NBT.TAG_COMPOUND)) {
            setter.accept(GasStack.readFromNBT(nbt.getCompound(key)));
        }
    }

    public static void setInfuseTypeIfPresent(CompoundNBT nbt, String key, Consumer<InfuseType> setter) {
        if (nbt.contains(key, NBT.TAG_COMPOUND)) {
            setter.accept(InfuseType.readFromNBT(nbt.getCompound(key)));
        }
    }

    public static void setInfusionStackIfPresent(CompoundNBT nbt, String key, Consumer<InfusionStack> setter) {
        if (nbt.contains(key, NBT.TAG_COMPOUND)) {
            setter.accept(InfusionStack.readFromNBT(nbt.getCompound(key)));
        }
    }

    public static void setPigmentIfPresent(CompoundNBT nbt, String key, Consumer<Pigment> setter) {
        if (nbt.contains(key, NBT.TAG_COMPOUND)) {
            setter.accept(Pigment.readFromNBT(nbt.getCompound(key)));
        }
    }

    public static void setPigmentStackIfPresent(CompoundNBT nbt, String key, Consumer<PigmentStack> setter) {
        if (nbt.contains(key, NBT.TAG_COMPOUND)) {
            setter.accept(PigmentStack.readFromNBT(nbt.getCompound(key)));
        }
    }

    public static void setSlurryIfPresent(CompoundNBT nbt, String key, Consumer<Slurry> setter) {
        if (nbt.contains(key, NBT.TAG_COMPOUND)) {
            setter.accept(Slurry.readFromNBT(nbt.getCompound(key)));
        }
    }

    public static void setSlurryStackIfPresent(CompoundNBT nbt, String key, Consumer<SlurryStack> setter) {
        if (nbt.contains(key, NBT.TAG_COMPOUND)) {
            setter.accept(SlurryStack.readFromNBT(nbt.getCompound(key)));
        }
    }

    public static void setFloatingLongIfPresent(CompoundNBT nbt, String key, FloatingLongConsumer setter) {
        if (nbt.contains(key, NBT.TAG_STRING)) {
            try {
                setter.accept(FloatingLong.parseFloatingLong(nbt.getString(key)));
            } catch (NumberFormatException e) {
                setter.accept(FloatingLong.ZERO);
            }
        }
    }

    public static void setItemStackIfPresent(CompoundNBT nbt, String key, Consumer<ItemStack> setter) {
        if (nbt.contains(key, NBT.TAG_COMPOUND)) {
            setter.accept(ItemStack.read(nbt.getCompound(key)));
        }
    }

    public static void setResourceLocationIfPresent(CompoundNBT nbt, String key, Consumer<ResourceLocation> setter) {
        if (nbt.contains(key, NBT.TAG_STRING)) {
            ResourceLocation value = ResourceLocation.tryCreate(nbt.getString(key));
            if (value != null) {
                setter.accept(value);
            }
        }
    }

    public static <ENUM extends Enum<ENUM>> void setEnumIfPresent(CompoundNBT nbt, String key, Int2ObjectFunction<ENUM> indexLookup, Consumer<ENUM> setter) {
        if (nbt.contains(key, NBT.TAG_INT)) {
            setter.accept(indexLookup.apply(nbt.getInt(key)));
        }
    }
}