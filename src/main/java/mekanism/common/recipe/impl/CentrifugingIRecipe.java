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
public class CentrifugingIRecipe extends BasicGasToGasRecipe implements ChemicalOutputInternal<Gas, GasStack> {

    public CentrifugingIRecipe(GasStackIngredient input, GasStack output) {
        super(input, output, MekanismRecipeTypes.TYPE_CENTRIFUGING.get());
    }

    @Override
    public RecipeType<GasToGasRecipe> getType() {
        return MekanismRecipeType.CENTRIFUGING.get();
    }

    @Override
    public RecipeSerializer<CentrifugingIRecipe> getSerializer() {
        return MekanismRecipeSerializers.CENTRIFUGING.get();
    }

    @Override
    public String getGroup() {
        return "isotopic_centrifuge";
    }

    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.ISOTOPIC_CENTRIFUGE.getItemStack();
    }

    @Override
    public GasStack getOutputRaw() {
        return output;
    }
}