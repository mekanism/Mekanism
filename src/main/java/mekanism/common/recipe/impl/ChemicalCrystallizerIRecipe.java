package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.inputs.chemical.IChemicalStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;

public class ChemicalCrystallizerIRecipe extends ChemicalCrystallizerRecipe {

    public ChemicalCrystallizerIRecipe(ResourceLocation id, IChemicalStackIngredient<?, ?> input, ItemStack output) {
        super(id, input, output);
    }

    @Nonnull
    @Override
    public IRecipeType<ChemicalCrystallizerRecipe> getType() {
        return MekanismRecipeType.CRYSTALLIZING;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<ChemicalCrystallizerRecipe> getSerializer() {
        return MekanismRecipeSerializers.CRYSTALLIZING.getRecipeSerializer();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlocks.CHEMICAL_CRYSTALLIZER.getName();
    }

    @Nonnull
    @Override
    public ItemStack getIcon() {
        return MekanismBlocks.CHEMICAL_CRYSTALLIZER.getItemStack();
    }
}