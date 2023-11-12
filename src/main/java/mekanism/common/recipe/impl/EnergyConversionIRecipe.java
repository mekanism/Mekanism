package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.basic.BasicItemStackToEnergyRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

@NothingNullByDefault
public class EnergyConversionIRecipe extends BasicItemStackToEnergyRecipe {

    public EnergyConversionIRecipe(ItemStackIngredient input, FloatingLong output) {
        super(input, output);
    }

    @Override
    public RecipeSerializer<EnergyConversionIRecipe> getSerializer() {
        return MekanismRecipeSerializers.ENERGY_CONVERSION.get();
    }

    @Override
    public String getGroup() {
        return "energy_conversion";
    }

    @Override
    public ItemStack getToastSymbol() {
        return MekanismItems.ENERGY_TABLET.getItemStack();
    }
}