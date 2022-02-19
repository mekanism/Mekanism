package mekanism.common.recipe.serializer;

import com.google.gson.JsonObject;
import java.util.function.Function;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.recipe.WrappedShapedRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class WrappedShapedRecipeSerializer<RECIPE extends WrappedShapedRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<RECIPE> {

    private final Function<ShapedRecipe, RECIPE> wrapper;

    public WrappedShapedRecipeSerializer(Function<ShapedRecipe, RECIPE> wrapper) {
        this.wrapper = wrapper;
    }

    @Nonnull
    @Override
    public RECIPE fromJson(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
        return wrapper.apply(IRecipeSerializer.SHAPED_RECIPE.fromJson(recipeId, json));
    }

    @Override
    public RECIPE fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer) {
        try {
            return wrapper.apply(IRecipeSerializer.SHAPED_RECIPE.fromNetwork(recipeId, buffer));
        } catch (Exception e) {
            Mekanism.logger.error("Error reading wrapped shaped recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void toNetwork(@Nonnull PacketBuffer buffer, @Nonnull RECIPE recipe) {
        try {
            IRecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe.getInternal());
        } catch (Exception e) {
            Mekanism.logger.error("Error writing wrapped shaped recipe to packet.", e);
            throw e;
        }
    }
}