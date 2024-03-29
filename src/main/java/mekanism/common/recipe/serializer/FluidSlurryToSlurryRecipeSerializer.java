package mekanism.common.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.JsonConstants;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.FluidSlurryToSlurryRecipe;
import mekanism.api.recipes.basic.BasicFluidSlurryToSlurryRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.SlurryStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class FluidSlurryToSlurryRecipeSerializer implements RecipeSerializer<BasicFluidSlurryToSlurryRecipe> {

    private final IFactory<BasicFluidSlurryToSlurryRecipe> factory;
    private Codec<BasicFluidSlurryToSlurryRecipe> codec;

    public FluidSlurryToSlurryRecipeSerializer(IFactory<BasicFluidSlurryToSlurryRecipe> factory) {
        this.factory = factory;
    }

    @NotNull
    @Override
    public Codec<BasicFluidSlurryToSlurryRecipe> codec() {
        if (codec == null) {
            codec = RecordCodecBuilder.create(instance -> instance.group(
                  IngredientCreatorAccess.fluid().codec().fieldOf(JsonConstants.FLUID_INPUT).forGetter(FluidSlurryToSlurryRecipe::getFluidInput),
                  IngredientCreatorAccess.slurry().codec().fieldOf(JsonConstants.SLURRY_INPUT).forGetter(FluidSlurryToSlurryRecipe::getChemicalInput),
                  ChemicalUtils.SLURRY_STACK_CODEC.fieldOf(JsonConstants.OUTPUT).forGetter(BasicFluidSlurryToSlurryRecipe::getOutputRaw)
            ).apply(instance, factory::create));
        }
        return codec;
    }

    @NotNull
    @Override
    public BasicFluidSlurryToSlurryRecipe fromNetwork(@NotNull FriendlyByteBuf buffer) {
        FluidStackIngredient fluidInput = IngredientCreatorAccess.fluid().read(buffer);
        SlurryStackIngredient slurryInput = IngredientCreatorAccess.slurry().read(buffer);
        SlurryStack output = SlurryStack.readFromPacket(buffer);
        return this.factory.create(fluidInput, slurryInput, output);
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull BasicFluidSlurryToSlurryRecipe recipe) {
        recipe.getFluidInput().write(buffer);
        recipe.getChemicalInput().write(buffer);
        recipe.getOutputRaw().writeToPacket(buffer);
    }

    @FunctionalInterface
    public interface IFactory<RECIPE extends FluidSlurryToSlurryRecipe> {

        RECIPE create(FluidStackIngredient fluidInput, SlurryStackIngredient slurryInput, SlurryStack output);
    }
}