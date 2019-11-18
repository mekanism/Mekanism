package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.MekanismBlock;
import mekanism.common.recipe.MekanismRecipeSerializers;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;

public class CombinerIRecipe extends CombinerRecipe {

    public CombinerIRecipe(ResourceLocation id, ItemStackIngredient mainInput, ItemStackIngredient extraInput, ItemStack output) {
        super(id, mainInput, extraInput, output);
    }

    @Nonnull
    @Override
    public IRecipeType<CombinerRecipe> getType() {
        return MekanismRecipeType.COMBINING;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<CombinerRecipe> getSerializer() {
        return MekanismRecipeSerializers.COMBINING.getRecipeSerializer();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlock.COMBINER.getName();
    }

    @Nonnull
    @Override
    public ItemStack getIcon() {
        return MekanismBlock.COMBINER.getItemStack();
    }
}