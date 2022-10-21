package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.FluidSlurryToSlurryRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.SlurryStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

@NothingNullByDefault
public class FluidSlurryToSlurryIRecipe extends FluidSlurryToSlurryRecipe {

    public FluidSlurryToSlurryIRecipe(ResourceLocation id, FluidStackIngredient fluidInput, SlurryStackIngredient slurryInput, SlurryStack output) {
        super(id, fluidInput, slurryInput, output);
    }

    @Override
    public RecipeType<FluidSlurryToSlurryRecipe> getType() {
        return MekanismRecipeType.WASHING.get();
    }

    @Override
    public RecipeSerializer<FluidSlurryToSlurryRecipe> getSerializer() {
        return MekanismRecipeSerializers.WASHING.get();
    }

    @Override
    public String getGroup() {
        return MekanismBlocks.CHEMICAL_WASHER.getName();
    }

    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.CHEMICAL_WASHER.getItemStack();
    }
}