package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class FluidToFluidIRecipe extends FluidToFluidRecipe {

    public FluidToFluidIRecipe(ResourceLocation id, FluidStackIngredient input, FluidStack output) {
        super(id, input, output);
    }

    @Nonnull
    @Override
    public IRecipeType<FluidToFluidRecipe> getType() {
        return MekanismRecipeType.EVAPORATING;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<FluidToFluidRecipe> getSerializer() {
        return MekanismRecipeSerializers.EVAPORATING.getRecipeSerializer();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER.getName();
    }

    @Nonnull
    @Override
    public ItemStack getIcon() {
        return MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER.getItemStack();
    }
}