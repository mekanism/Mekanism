package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.resources.ResourceLocation;

public class ChemicalDissolutionIRecipe extends ChemicalDissolutionRecipe {

    public ChemicalDissolutionIRecipe(ResourceLocation id, ItemStackIngredient itemInput, GasStackIngredient gasInput, ChemicalStack<?> output) {
        super(id, itemInput, gasInput, output);
    }

    @Nonnull
    @Override
    public RecipeType<ChemicalDissolutionRecipe> getType() {
        return MekanismRecipeType.DISSOLUTION;
    }

    @Nonnull
    @Override
    public RecipeSerializer<ChemicalDissolutionRecipe> getSerializer() {
        return MekanismRecipeSerializers.DISSOLUTION.get();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER.getName();
    }

    @Nonnull
    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER.getItemStack();
    }
}