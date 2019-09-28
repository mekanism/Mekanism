package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.MekanismBlock;
import mekanism.common.recipe.MekanismRecipeSerializers;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;

public class CrushingIRecipe extends ItemStackToItemStackIRecipe {

    public CrushingIRecipe(ResourceLocation id, ItemStackIngredient input, ItemStack output) {
        super(id, input, output);
    }

    @Nonnull
    @Override
    public IRecipeType<ItemStackToItemStackIRecipe> getType() {
        return MekanismRecipeType.CRUSHER;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<ItemStackToItemStackIRecipe> getSerializer() {
        return MekanismRecipeSerializers.CRUSHER;
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlock.CRUSHER.getName();
    }

    @Nonnull
    @Override
    public ItemStack getIcon() {
        return MekanismBlock.CRUSHER.getItemStack();
    }
}