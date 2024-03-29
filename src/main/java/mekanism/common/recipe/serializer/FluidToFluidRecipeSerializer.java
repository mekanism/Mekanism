package mekanism.common.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.basic.BasicFluidToFluidRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class FluidToFluidRecipeSerializer<RECIPE extends BasicFluidToFluidRecipe> implements RecipeSerializer<RECIPE> {

    private final IFactory<RECIPE> factory;
    private Codec<RECIPE> codec;

    public FluidToFluidRecipeSerializer(IFactory<RECIPE> factory) {
        this.factory = factory;
    }

    @NotNull
    @Override
    public Codec<RECIPE> codec() {
        if (codec == null) {
            codec = RecordCodecBuilder.create(instance -> instance.group(
                  IngredientCreatorAccess.fluid().codec().fieldOf(JsonConstants.INPUT).forGetter(FluidToFluidRecipe::getInput),
                  SerializerHelper.FLUIDSTACK_CODEC.fieldOf(JsonConstants.OUTPUT).forGetter(BasicFluidToFluidRecipe::getOutputRaw)
            ).apply(instance, factory::create));
        }
        return codec;
    }

    @NotNull
    @Override
    public RECIPE fromNetwork(@NotNull FriendlyByteBuf buffer) {
        FluidStackIngredient inputIngredient = IngredientCreatorAccess.fluid().read(buffer);
        FluidStack output = FluidStack.readFromPacket(buffer);
        return this.factory.create(inputIngredient, output);
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
        recipe.getInput().write(buffer);
        recipe.getOutputRaw().writeToPacket(buffer);
    }

    @FunctionalInterface
    public interface IFactory<RECIPE extends FluidToFluidRecipe> {

        RECIPE create(FluidStackIngredient input, FluidStack output);
    }
}