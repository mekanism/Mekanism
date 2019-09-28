package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.MekanismBlock;
import mekanism.common.recipe.MekanismRecipeSerializers;
import mekanism.common.recipe.MekanismRecipeType;
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
        return MekanismRecipeSerializers.SMELTING;
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlock.ENERGIZED_SMELTER.getName();
    }

    @Nonnull
    @Override
    public ItemStack getIcon() {
        return MekanismBlock.ENERGIZED_SMELTER.getItemStack();
    }
}