package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.recipes.PaintingRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.PigmentStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.resources.ResourceLocation;

public class PaintingIRecipe extends PaintingRecipe {

    public PaintingIRecipe(ResourceLocation id, ItemStackIngredient itemInput, PigmentStackIngredient pigmentInput, ItemStack output) {
        super(id, itemInput, pigmentInput, output);
    }

    @Nonnull
    @Override
    public RecipeType<PaintingRecipe> getType() {
        return MekanismRecipeType.PAINTING;
    }

    @Nonnull
    @Override
    public RecipeSerializer<PaintingRecipe> getSerializer() {
        return MekanismRecipeSerializers.PAINTING.get();
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