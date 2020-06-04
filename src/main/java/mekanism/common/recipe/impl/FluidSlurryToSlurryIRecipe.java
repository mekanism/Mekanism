package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.FluidSlurryToSlurryRecipe;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.chemical.SlurryStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;

public class FluidSlurryToSlurryIRecipe extends FluidSlurryToSlurryRecipe {

    public FluidSlurryToSlurryIRecipe(ResourceLocation id, FluidStackIngredient fluidInput, SlurryStackIngredient slurryInput, SlurryStack output) {
        super(id, fluidInput, slurryInput, output);
    }

    @Nonnull
    @Override
    public IRecipeType<FluidSlurryToSlurryRecipe> getType() {
        return MekanismRecipeType.WASHING;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<FluidSlurryToSlurryRecipe> getSerializer() {
        return MekanismRecipeSerializers.WASHING.getRecipeSerializer();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlocks.CHEMICAL_WASHER.getName();
    }

    @Nonnull
    @Override
    public ItemStack getIcon() {
        return MekanismBlocks.CHEMICAL_WASHER.getItemStack();
    }
}