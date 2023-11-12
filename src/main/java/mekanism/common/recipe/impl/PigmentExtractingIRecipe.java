package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
import mekanism.api.recipes.basic.BasicItemStackToPigmentRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

@NothingNullByDefault
public class PigmentExtractingIRecipe extends BasicItemStackToPigmentRecipe implements ChemicalOutputInternal<Pigment, PigmentStack> {

    public PigmentExtractingIRecipe(ItemStackIngredient input, PigmentStack output) {
        super(input, output);
    }

    @Override
    public RecipeType<ItemStackToPigmentRecipe> getType() {
        return MekanismRecipeType.PIGMENT_EXTRACTING.get();
    }

    @Override
    public RecipeSerializer<PigmentExtractingIRecipe> getSerializer() {
        return MekanismRecipeSerializers.PIGMENT_EXTRACTING.get();
    }

    @Override
    public String getGroup() {
        return MekanismBlocks.PIGMENT_EXTRACTOR.getName();
    }

    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.PIGMENT_EXTRACTOR.getItemStack();
    }

    @Override
    public PigmentStack getOutputRaw() {
        return output;
    }
}