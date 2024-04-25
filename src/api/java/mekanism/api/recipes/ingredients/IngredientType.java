package mekanism.api.recipes.ingredients;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

//TODO - 1.20.5: JAVADOCS
public enum IngredientType {
    SINGLE,
    TAGGED,
    MULTI;

    public static final IntFunction<IngredientType> BY_ID = ByIdMap.continuous(IngredientType::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    public static final StreamCodec<ByteBuf, IngredientType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, IngredientType::ordinal);
}