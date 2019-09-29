package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.common.MekanismBlock;
import mekanism.common.recipe.MekanismRecipeSerializers;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;

public class GasToGasIRecipe extends GasToGasRecipe {

    public GasToGasIRecipe(ResourceLocation id, GasStackIngredient input, GasStack output) {
        super(id, input, output);
    }

    @Nonnull
    @Override
    public IRecipeType<GasToGasRecipe> getType() {
        return MekanismRecipeType.ACTIVATING;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<GasToGasRecipe> getSerializer() {
        return MekanismRecipeSerializers.ACTIVATING;
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlock.SOLAR_NEUTRON_ACTIVATOR.getName();
    }

    @Nonnull
    @Override
    public ItemStack getIcon() {
        return MekanismBlock.SOLAR_NEUTRON_ACTIVATOR.getItemStack();
    }
}