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
public class GasConversionIRecipe extends BasicItemStackToGasRecipe implements ChemicalOutputInternal<Gas, GasStack> {

    public GasConversionIRecipe(ItemStackIngredient input, GasStack output) {
        super(input, output, MekanismRecipeTypes.TYPE_GAS_CONVERSION.get());
    }

    @Override
    public RecipeSerializer<GasConversionIRecipe> getSerializer() {
        return MekanismRecipeSerializers.GAS_CONVERSION.get();
    }

    @Override
    public String getGroup() {
        return "gas_conversion";
    }

    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.CREATIVE_CHEMICAL_TANK.getItemStack();
    }

    @Override
    public GasStack getOutputRaw() {
        return output;
    }
}