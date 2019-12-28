package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;

public class SawmillIRecipe extends SawmillRecipe {

    public SawmillIRecipe(ResourceLocation id, ItemStackIngredient input, ItemStack mainOutput, ItemStack secondaryOutput, double secondaryChance) {
        super(id, input, mainOutput, secondaryOutput, secondaryChance);
    }

    @Nonnull
    @Override
    public IRecipeType<SawmillRecipe> getType() {
        return MekanismRecipeType.SAWING;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<SawmillRecipe> getSerializer() {
        return MekanismRecipeSerializers.SAWING.getRecipeSerializer();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlocks.PRECISION_SAWMILL.getName();
    }

    @Nonnull
    @Override
    public ItemStack getIcon() {
        return MekanismBlocks.PRECISION_SAWMILL.getItemStack();
    }
}