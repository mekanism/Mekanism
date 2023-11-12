package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.basic.BasicGasToGasRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

@NothingNullByDefault
public class ActivatingIRecipe extends BasicGasToGasRecipe implements ChemicalOutputInternal<Gas, GasStack> {

    public ActivatingIRecipe(GasStackIngredient input, GasStack output) {
        super(input, output, MekanismRecipeTypes.TYPE_ACTIVATING.get());
    }

    @Override
    public RecipeType<GasToGasRecipe> getType() {
        return MekanismRecipeType.ACTIVATING.get();
    }

    @Override
    public RecipeSerializer<ActivatingIRecipe> getSerializer() {
        return MekanismRecipeSerializers.ACTIVATING.get();
    }

    @Override
    public String getGroup() {
        return "solar_neutron_activator";
    }

    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR.getItemStack();
    }

    @Override
    public GasStack getOutputRaw() {
        return output;
    }
}