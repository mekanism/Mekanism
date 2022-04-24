package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.FluidStack;

public class FluidToFluidIRecipe extends FluidToFluidRecipe {

    public FluidToFluidIRecipe(ResourceLocation id, FluidStackIngredient input, FluidStack output) {
        super(id, input, output);
    }

    @Nonnull
    @Override
    public RecipeType<FluidToFluidRecipe> getType() {
        return MekanismRecipeType.EVAPORATING.get();
    }

    @Nonnull
    @Override
    public RecipeSerializer<FluidToFluidRecipe> getSerializer() {
        return MekanismRecipeSerializers.EVAPORATING.get();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER.getName();
    }

    @Nonnull
    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER.getItemStack();
    }
}