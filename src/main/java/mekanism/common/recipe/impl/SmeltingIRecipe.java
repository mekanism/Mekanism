package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;

public class SmeltingIRecipe extends ItemStackToItemStackRecipe {

    public SmeltingIRecipe(ResourceLocation id, ItemStackIngredient input, ItemStack output) {
        super(id, input, output);
    }

    @Nonnull
    @Override
    public IRecipeType<ItemStackToItemStackRecipe> getType() {
        return MekanismRecipeType.SMELTING;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<ItemStackToItemStackRecipe> getSerializer() {
        return MekanismRecipeSerializers.SMELTING.getRecipeSerializer();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlocks.ENERGIZED_SMELTER.getName();
    }

    @Nonnull
    @Override
    public ItemStack getIcon() {
        return MekanismBlocks.ENERGIZED_SMELTER.getItemStack();
    }
}