package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;

public class PigmentExtractingIRecipe extends ItemStackToPigmentRecipe {

    public PigmentExtractingIRecipe(ResourceLocation id, ItemStackIngredient input, PigmentStack output) {
        super(id, input, output);
    }

    @Nonnull
    @Override
    public IRecipeType<ItemStackToPigmentRecipe> getType() {
        return MekanismRecipeType.PIGMENT_EXTRACTING;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<ItemStackToPigmentRecipe> getSerializer() {
        return MekanismRecipeSerializers.PIGMENT_EXTRACTING.getRecipeSerializer();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlocks.PIGMENT_EXTRACTOR.getName();
    }

    @Nonnull
    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.PIGMENT_EXTRACTOR.getItemStack();
    }
}