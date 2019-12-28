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

public class CrushingIRecipe extends ItemStackToItemStackRecipe {

    public CrushingIRecipe(ResourceLocation id, ItemStackIngredient input, ItemStack output) {
        super(id, input, output);
    }

    @Nonnull
    @Override
    public IRecipeType<ItemStackToItemStackRecipe> getType() {
        return MekanismRecipeType.CRUSHING;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<ItemStackToItemStackRecipe> getSerializer() {
        return MekanismRecipeSerializers.CRUSHING.getRecipeSerializer();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlocks.CRUSHER.getName();
    }

    @Nonnull
    @Override
    public ItemStack getIcon() {
        return MekanismBlocks.CRUSHER.getItemStack();
    }
}