package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;

public class CentrifugingIRecipe extends GasToGasRecipe {

    public CentrifugingIRecipe(ResourceLocation id, GasStackIngredient input, GasStack output) {
        super(id, input, output);
    }

    @Nonnull
    @Override
    public IRecipeType<GasToGasRecipe> getType() {
        return MekanismRecipeType.CENTRIFUGING;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<GasToGasRecipe> getSerializer() {
        return MekanismRecipeSerializers.CENTRIFUGING.getRecipeSerializer();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlocks.ISOTOPIC_CENTRIFUGE.getName();
    }

    @Nonnull
    @Override
    public ItemStack getIcon() {
        return MekanismBlocks.ISOTOPIC_CENTRIFUGE.getItemStack();
    }
}
