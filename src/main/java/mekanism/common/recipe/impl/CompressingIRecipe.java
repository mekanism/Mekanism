package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismMachines;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;

public class CompressingIRecipe extends ItemStackGasToItemStackRecipe {

    public CompressingIRecipe(ResourceLocation id, ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output) {
        super(id, itemInput, gasInput, output);
    }

    @Nonnull
    @Override
    public IRecipeType<ItemStackGasToItemStackRecipe> getType() {
        return MekanismRecipeType.COMPRESSING;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<ItemStackGasToItemStackRecipe> getSerializer() {
        return MekanismRecipeSerializers.COMPRESSING.getRecipeSerializer();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismMachines.OSMIUM_COMPRESSOR.getName();
    }

    @Nonnull
    @Override
    public ItemStack getIcon() {
        return MekanismMachines.OSMIUM_COMPRESSOR.getBlockType().getItemStack();
    }
}