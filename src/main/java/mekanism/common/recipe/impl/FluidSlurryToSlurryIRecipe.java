package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.basic.BasicFluidSlurryToSlurryRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.SlurryStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

@NothingNullByDefault
public class FluidSlurryToSlurryIRecipe extends BasicFluidSlurryToSlurryRecipe {

    public FluidSlurryToSlurryIRecipe(FluidStackIngredient fluidInput, SlurryStackIngredient slurryInput, SlurryStack output) {
        super(fluidInput, slurryInput, output);
    }

    @Override
    public RecipeSerializer<FluidSlurryToSlurryIRecipe> getSerializer() {
        return MekanismRecipeSerializers.WASHING.get();
    }

    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.CHEMICAL_WASHER.getItemStack();
    }

    public SlurryStack getOutputRaw() {
        return output;
    }
}