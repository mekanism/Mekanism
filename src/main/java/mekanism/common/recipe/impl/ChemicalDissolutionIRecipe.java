package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

@NothingNullByDefault
public class ChemicalDissolutionIRecipe extends ChemicalDissolutionRecipe {

    public ChemicalDissolutionIRecipe(ResourceLocation id, ItemStackIngredient itemInput, GasStackIngredient gasInput, ChemicalStack<?> output) {
        super(id, itemInput, gasInput, output);
    }

    @Override
    public RecipeType<ChemicalDissolutionRecipe> getType() {
        return MekanismRecipeType.DISSOLUTION.get();
    }

    @Override
    public RecipeSerializer<ChemicalDissolutionRecipe> getSerializer() {
        return MekanismRecipeSerializers.DISSOLUTION.get();
    }

    @Override
    public String getGroup() {
        return MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER.getName();
    }

    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER.getItemStack();
    }
}