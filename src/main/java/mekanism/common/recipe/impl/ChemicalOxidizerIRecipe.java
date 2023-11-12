package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.api.recipes.basic.BasicItemStackToGasRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

@NothingNullByDefault
public class ChemicalOxidizerIRecipe extends BasicItemStackToGasRecipe implements ChemicalOutputInternal<Gas, GasStack> {

    public ChemicalOxidizerIRecipe(ItemStackIngredient input, GasStack output) {
        super(input, output);
    }

    @Override
    public RecipeType<ItemStackToGasRecipe> getType() {
        return MekanismRecipeType.OXIDIZING.get();
    }

    @Override
    public RecipeSerializer<ChemicalOxidizerIRecipe> getSerializer() {
        return MekanismRecipeSerializers.OXIDIZING.get();
    }

    @Override
    public String getGroup() {
        return MekanismBlocks.CHEMICAL_OXIDIZER.getName();
    }

    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.CHEMICAL_OXIDIZER.getItemStack();
    }

    @Override
    public GasStack getOutputRaw() {
        return output;
    }
}