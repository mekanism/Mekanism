package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;

public class EnergyConversionIRecipe extends ItemStackToEnergyRecipe {

    public EnergyConversionIRecipe(ResourceLocation id, ItemStackIngredient input, FloatingLong output) {
        super(id, input, output);
    }

    @Nonnull
    @Override
    public IRecipeType<ItemStackToEnergyRecipe> getType() {
        return MekanismRecipeType.ENERGY_CONVERSION;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<ItemStackToEnergyRecipe> getSerializer() {
        return MekanismRecipeSerializers.ENERGY_CONVERSION.getRecipeSerializer();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return "energy_conversion";
    }

    @Nonnull
    @Override
    public ItemStack getIcon() {
        return MekanismItems.ENERGY_TABLET.getItemStack();
    }
}