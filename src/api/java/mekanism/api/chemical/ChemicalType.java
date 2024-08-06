package mekanism.api.chemical;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Map;
import java.util.function.IntFunction;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.chemical.IChemicalIngredient;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO: Make the chemicals know their own chemical type
public enum ChemicalType implements StringRepresentable {
    GAS("gas"),
    INFUSION("infuse_type"),
    PIGMENT("pigment"),
    SLURRY("slurry"),
    CHEMICAL("chemical");

    private static final Map<String, ChemicalType> nameToType;

    static {
        ChemicalType[] values = values();
        nameToType = new Object2ObjectArrayMap<>(values.length);
        for (ChemicalType type : values) {
            nameToType.put(type.getSerializedName(), type);
        }
    }

    /**
     * Codec for serializing chemical types based on their name.
     *
     * @since 10.6.0
     */
    public static final Codec<ChemicalType> CODEC = StringRepresentable.fromEnum(ChemicalType::values);
    /**
     * Gets a chemical type by index, wrapping for out of bounds indices.
     *
     * @since 10.6.0
     */
    public static final IntFunction<ChemicalType> BY_ID = ByIdMap.continuous(ChemicalType::ordinal, values(), OutOfBoundsStrategy.WRAP);
    /**
     * Stream codec for syncing chemical types by index.
     *
     * @since 10.6.0
     */
    public static final StreamCodec<ByteBuf, ChemicalType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ChemicalType::ordinal);

    private final String name;

    ChemicalType(String name) {
        this.name = name;
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
    public boolean isInstance(Chemical chemical) {
        return this == CHEMICAL;
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
     * Gets the Chemical Type of a chemical.
     *
     * @param chemical Chemical.
     *
     * @return Chemical Type.
     */
    public static ChemicalType getTypeFor(Chemical chemical) {
        return CHEMICAL;
    }

    /**
     * Gets the Chemical Type of a chemical stack.
     *
     * @param stack Stack.
     *
     * @return Chemical Type.
     */
    public static ChemicalType getTypeFor(ChemicalStack stack) {
        return getTypeFor(stack.getChemical());
    }

    /**
     * Gets the Chemical Type of a chemical stack ingredient.
     *
     * @param ingredient Ingredient.
     *
     * @return Chemical Type.
     */
    public static ChemicalType getTypeFor(ChemicalStackIngredient ingredient) {
        return CHEMICAL;
    }

    /**
     * Gets the Chemical Type of a chemical ingredient.
     *
     * @param ingredient Ingredient.
     *
     * @return Chemical Type.
     *
     * @since 10.6.0
     */
    public static ChemicalType getTypeFor(IChemicalIngredient ingredient) {
        return CHEMICAL;
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
    public static ChemicalType getTypeFor(IChemicalHandler handler) {
        return CHEMICAL;
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
    public static ChemicalType getTypeFor(IChemicalTank tank) {
        return CHEMICAL;
    }
}