package mekanism.common.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.function.Function;
import java.util.stream.Stream;
import mekanism.common.Mekanism;
import mekanism.common.recipe.WrappedShapedRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.jetbrains.annotations.NotNull;

public class WrappedShapedRecipeSerializer<RECIPE extends WrappedShapedRecipe> implements RecipeSerializer<RECIPE> {

    private final Function<ShapedRecipe, RECIPE> wrapper;
    private Codec<RECIPE> codec;

    public WrappedShapedRecipeSerializer(Function<ShapedRecipe, RECIPE> wrapper) {
        this.wrapper = wrapper;
    }

    @NotNull
    @Override
    public Codec<RECIPE> codec() {
        if (codec == null) {
            codec = new WrappingMapCodec<>(RecipeSerializer.SHAPED_RECIPE.codec(), wrapper, WrappedShapedRecipe::getInternal).codec();
        }
        return codec;
    }

    @NotNull
    @Override
    public RECIPE fromNetwork(@NotNull FriendlyByteBuf buffer) {
        try {
            return wrapper.apply(RecipeSerializer.SHAPED_RECIPE.fromNetwork(buffer));
        } catch (Exception e) {
            Mekanism.logger.error("Error reading wrapped shaped recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
        try {
            RecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe.getInternal());
        } catch (Exception e) {
            Mekanism.logger.error("Error writing wrapped shaped recipe to packet.", e);
            throw e;
        }
    }

    private static class WrappingMapCodec<TYPE, TARGET_TYPE> extends MapCodec<TARGET_TYPE> {

        private final MapCodec<TYPE> codec;
        private final Function<TYPE, TARGET_TYPE> wrapper;
        private final Function<TARGET_TYPE, TYPE> unwrapper;

        public WrappingMapCodec(Codec<TYPE> codec, Function<TYPE, TARGET_TYPE> wrapper, Function<TARGET_TYPE, TYPE> unwrapper) {
            this.codec = ((MapCodecCodec<TYPE>) codec).codec();
            this.wrapper = wrapper;
            this.unwrapper = unwrapper;
        }

        @Override
        public <T> DataResult<TARGET_TYPE> decode(final DynamicOps<T> ops, final MapLike<T> input) {
            return codec.decode(ops, input).map(wrapper);
        }

        @Override
        public <T> RecordBuilder<T> encode(final TARGET_TYPE input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
            return codec.encode(unwrapper.apply(input), ops, prefix);
        }

        @Override
        public <T> Stream<T> keys(final DynamicOps<T> ops) {
            return codec.keys(ops);
        }

        @Override
        public String toString() {
            return "mekanism:WrappingMapCodec[" + codec + "]";
        }
    }
}