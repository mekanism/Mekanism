package mekanism.common.recipe.serializer;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import java.util.function.Function;
import mekanism.common.Mekanism;
import mekanism.common.recipe.WrappedShapedRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
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
            codec = RecipeSerializer.SHAPED_RECIPE.codec().xmap(wrapper, WrappedShapedRecipe::getInternal);
        }
        return codec;
    }

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
}