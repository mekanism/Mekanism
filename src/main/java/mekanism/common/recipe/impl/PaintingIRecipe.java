package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.recipes.PaintingRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.PigmentStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;

public class PaintingIRecipe extends PaintingRecipe {

    public PaintingIRecipe(ResourceLocation id, ItemStackIngredient itemInput, PigmentStackIngredient pigmentInput, ItemStack output) {
        super(id, itemInput, pigmentInput, output);
    }

    @Nonnull
    @Override
    public IRecipeType<PaintingRecipe> getType() {
        return MekanismRecipeType.PAINTING;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<PaintingRecipe> getSerializer() {
        return MekanismRecipeSerializers.PAINTING.getRecipeSerializer();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlocks.PAINTING_MACHINE.getName();
    }

    @Nonnull
    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.PAINTING_MACHINE.getItemStack();
    }
}