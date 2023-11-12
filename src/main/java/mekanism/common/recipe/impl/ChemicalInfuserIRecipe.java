package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.basic.BasicChemicalInfuserRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

@NothingNullByDefault
public class ChemicalInfuserIRecipe extends BasicChemicalInfuserRecipe implements ChemicalOutputInternal<Gas, GasStack> {

    public ChemicalInfuserIRecipe(GasStackIngredient leftInput, GasStackIngredient rightInput, GasStack output) {
        super(leftInput, rightInput, output);
    }

    @Override
    public RecipeSerializer<ChemicalInfuserIRecipe> getSerializer() {
        return MekanismRecipeSerializers.CHEMICAL_INFUSING.get();
    }

    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.CHEMICAL_INFUSER.getItemStack();
    }

    @Override
    public GasStack getOutputRaw() {
        return output;
    }
}