package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.basic.BasicPaintingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.PigmentStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

@NothingNullByDefault
public class PaintingIRecipe extends BasicPaintingRecipe implements ItemStackOutputInternal {

    public PaintingIRecipe(ItemStackIngredient itemInput, PigmentStackIngredient pigmentInput, ItemStack output) {
        super(itemInput, pigmentInput, output);
    }

    @Override
    public RecipeSerializer<PaintingIRecipe> getSerializer() {
        return MekanismRecipeSerializers.PAINTING.get();
    }

    @Override
    public String getGroup() {
        return MekanismBlocks.PAINTING_MACHINE.getName();
    }

    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.PAINTING_MACHINE.getItemStack();
    }

    @Override
    public ItemStack getOutputRaw() {
        return output;
    }
}