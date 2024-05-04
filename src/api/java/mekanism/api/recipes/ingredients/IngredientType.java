package mekanism.api.recipes.ingredients;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

/**
 * Represents the implementation type of ingredients.
 *
 * @since 10.6.0
 */
public enum IngredientType {
    SINGLE,
    TAGGED,
    MULTI;

    /**
     * Gets an ingredient type by index, wrapping for out of bounds indices.
     */
    public static final IntFunction<IngredientType> BY_ID = ByIdMap.continuous(IngredientType::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    /**
     * Stream codec for syncing ingredient types by index.
     */
    public static final StreamCodec<ByteBuf, IngredientType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, IngredientType::ordinal);
}