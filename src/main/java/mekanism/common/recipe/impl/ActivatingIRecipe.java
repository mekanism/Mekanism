package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

@NothingNullByDefault
public class ActivatingIRecipe extends GasToGasRecipe {

    public ActivatingIRecipe(ResourceLocation id, GasStackIngredient input, GasStack output) {
        super(id, input, output);
    }

    @Override
    public RecipeType<GasToGasRecipe> getType() {
        return MekanismRecipeType.ACTIVATING.get();
    }

    @Override
    public RecipeSerializer<GasToGasRecipe> getSerializer() {
        return MekanismRecipeSerializers.ACTIVATING.get();
    }

    @Override
    public String getGroup() {
        return MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR.getName();
    }

    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR.getItemStack();
    }
}