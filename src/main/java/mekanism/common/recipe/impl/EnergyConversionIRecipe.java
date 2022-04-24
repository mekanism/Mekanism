package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class EnergyConversionIRecipe extends ItemStackToEnergyRecipe {

    public EnergyConversionIRecipe(ResourceLocation id, ItemStackIngredient input, FloatingLong output) {
        super(id, input, output);
    }

    @Nonnull
    @Override
    public RecipeType<ItemStackToEnergyRecipe> getType() {
        return MekanismRecipeType.ENERGY_CONVERSION.get();
    }

    @Nonnull
    @Override
    public RecipeSerializer<ItemStackToEnergyRecipe> getSerializer() {
        return MekanismRecipeSerializers.ENERGY_CONVERSION.get();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return "energy_conversion";
    }

    @Nonnull
    @Override
    public ItemStack getToastSymbol() {
        return MekanismItems.ENERGY_TABLET.getItemStack();
    }
}