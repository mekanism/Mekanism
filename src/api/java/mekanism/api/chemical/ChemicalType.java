package mekanism.api.chemical;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.InfusionStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.PigmentStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.SlurryStackIngredient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO: Make the chemicals know their own chemical type
public enum ChemicalType implements StringRepresentable {
    GAS("gas", c -> c instanceof Gas),
    INFUSION("infuse_type", c -> c instanceof InfuseType),
    PIGMENT("pigment", c -> c instanceof Pigment),
    SLURRY("slurry", c -> c instanceof Slurry);

    private static final Map<String, ChemicalType> nameToType;

    static {
        ChemicalType[] values = values();
        nameToType = new Object2ObjectArrayMap<>(values.length);
        for (ChemicalType type : values) {
            nameToType.put(type.getSerializedName(), type);
        }
    }

    //TODO - 1.20.5: Docs
    public static final Codec<ChemicalType> CODEC = StringRepresentable.fromEnum(ChemicalType::values);
    public static final IntFunction<ChemicalType> BY_ID = ByIdMap.continuous(ChemicalType::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    public static final StreamCodec<ByteBuf, ChemicalType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ChemicalType::ordinal);

    private final Predicate<Chemical<?>> instanceCheck;
    private final String name;

    ChemicalType(String name, Predicate<Chemical<?>> instanceCheck) {
        this.name = name;
        this.instanceCheck = instanceCheck;
    }

    @NotNull
    @Override
    public String getSerializedName() {
        return name;
    }

    /**
     * Checks if the given chemical is an instance of this Chemical Type.
     *
     * @param chemical Chemical to check.
     *
     * @return {@code true} if the given chemical is an instance.
     */
    public boolean isInstance(Chemical<?> chemical) {
        return instanceCheck.test(chemical);
    }

    /**
     * Writes a Chemical Type to NBT.
     *
     * @param nbt Tag to write to.
     */
    public void write(@NotNull CompoundTag nbt) {
        nbt.putString(NBTConstants.CHEMICAL_TYPE, getSerializedName());
    }

    /**
     * Gets a chemical type by name.
     *
     * @param name Name of the chemical type.
     *
     * @return Chemical Type.
     */
    @Nullable
    public static ChemicalType fromString(String name) {
        return nameToType.get(name);
    }

    /**
     * Reads a Chemical Type from NBT.
     *
     * @param nbt NBT.
     *
     * @return Chemical Type.
     */
    @Nullable
    public static ChemicalType fromNBT(@Nullable CompoundTag nbt) {
        if (nbt != null && nbt.contains(NBTConstants.CHEMICAL_TYPE, Tag.TAG_STRING)) {
            return fromString(nbt.getString(NBTConstants.CHEMICAL_TYPE));
        }
        return null;
    }

    /**
     * Gets the Chemical Type of a chemical.
     *
     * @param chemical Chemical.
     *
     * @return Chemical Type.
     */
    public static ChemicalType getTypeFor(Chemical<?> chemical) {
        if (chemical instanceof Gas) {
            return GAS;
        } else if (chemical instanceof InfuseType) {
            return INFUSION;
        } else if (chemical instanceof Pigment) {
            return PIGMENT;
        } else if (chemical instanceof Slurry) {
            return SLURRY;
        }
        throw new IllegalStateException("Unknown chemical type");
    }

    /**
     * Gets the Chemical Type of a chemical stack.
     *
     * @param stack Stack.
     *
     * @return Chemical Type.
     */
    public static ChemicalType getTypeFor(ChemicalStack<?> stack) {
        return getTypeFor(stack.getType());
    }

    /**
     * Gets the Chemical Type of a chemical stack ingredient.
     *
     * @param ingredient Ingredient.
     *
     * @return Chemical Type.
     */
    public static ChemicalType getTypeFor(ChemicalStackIngredient<?, ?> ingredient) {
        if (ingredient instanceof GasStackIngredient) {
            return GAS;
        } else if (ingredient instanceof InfusionStackIngredient) {
            return INFUSION;
        } else if (ingredient instanceof PigmentStackIngredient) {
            return PIGMENT;
        } else if (ingredient instanceof SlurryStackIngredient) {
            return SLURRY;
        }
        throw new IllegalStateException("Unknown chemical ingredient type");
    }

    /**
     * Gets the Chemical Type of a chemical handler.
     *
     * @param handler Handler.
     *
     * @return Chemical Type.
     *
     * @since 10.5.0
     */
    public static ChemicalType getTypeFor(IChemicalHandler<?, ?> handler) {
        if (handler instanceof IGasHandler) {
            return GAS;
        } else if (handler instanceof IInfusionHandler) {
            return INFUSION;
        } else if (handler instanceof IPigmentHandler) {
            return PIGMENT;
        } else if (handler instanceof ISlurryHandler) {
            return SLURRY;
        }
        throw new IllegalStateException("Unknown chemical handler type");
    }

    /**
     * Gets the Chemical Type of a chemical tank.
     *
     * @param tank Tank.
     *
     * @return Chemical Type.
     *
     * @since 10.5.0
     */
    public static ChemicalType getTypeFor(IChemicalTank<?, ?> tank) {
        if (tank instanceof IGasTank) {
            return GAS;
        } else if (tank instanceof IInfusionTank) {
            return INFUSION;
        } else if (tank instanceof IPigmentTank) {
            return PIGMENT;
        } else if (tank instanceof ISlurryTank) {
            return SLURRY;
        }
        throw new IllegalStateException("Unknown chemical tank type");
    }
}