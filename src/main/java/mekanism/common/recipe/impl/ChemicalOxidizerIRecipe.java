package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.basic.BasicItemStackToGasRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

@NothingNullByDefault
public class ChemicalOxidizerIRecipe extends BasicItemStackToGasRecipe implements ChemicalOutputInternal<Gas, GasStack> {

    public ChemicalOxidizerIRecipe(ItemStackIngredient input, GasStack output) {
        super(input, output, MekanismRecipeTypes.TYPE_OXIDIZING.get());
    }

    @Override
    public RecipeSerializer<ChemicalOxidizerIRecipe> getSerializer() {
        return MekanismRecipeSerializers.OXIDIZING.get();
    }

    @Override
    public String getGroup() {
        return "chemical_oxidizer";
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