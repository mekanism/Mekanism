package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.MekanismBlock;
import mekanism.common.recipe.MekanismRecipeSerializers;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;

public class PurifyingIRecipe extends ItemStackGasToItemStackIRecipe {

    public PurifyingIRecipe(ResourceLocation id, ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output) {
        super(id, itemInput, gasInput, output);
    }

    @Nonnull
    @Override
    public IRecipeType<ItemStackGasToItemStackIRecipe> getType() {
        return MekanismRecipeType.PURIFICATION_CHAMBER;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<ItemStackGasToItemStackIRecipe> getSerializer() {
        return MekanismRecipeSerializers.PURIFICATION_CHAMBER;
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlock.PURIFICATION_CHAMBER.getName();
    }

    @Nonnull
    @Override
    public ItemStack getIcon() {
        return MekanismBlock.PURIFICATION_CHAMBER.getItemStack();
    }
}