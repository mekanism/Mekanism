package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.basic.BasicItemStackGasToItemStackRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

@NothingNullByDefault
public class CompressingIRecipe extends BasicItemStackGasToItemStackRecipe implements ItemStackOutputInternal {

    public CompressingIRecipe(ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output) {
        super(itemInput, gasInput, output, MekanismRecipeTypes.TYPE_COMPRESSING.get());
    }

    @Override
    public RecipeSerializer<CompressingIRecipe> getSerializer() {
        return MekanismRecipeSerializers.COMPRESSING.get();
    }

    @Override
    public String getGroup() {
        return MekanismBlocks.OSMIUM_COMPRESSOR.getName();
    }

    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.OSMIUM_COMPRESSOR.getItemStack();
    }

    @Override
    public ItemStack getOutputRaw() {
        return output;
    }
}