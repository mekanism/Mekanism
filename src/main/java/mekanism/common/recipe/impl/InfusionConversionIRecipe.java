package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;

public class InfusionConversionIRecipe extends ItemStackToInfuseTypeRecipe {

    public InfusionConversionIRecipe(ResourceLocation id, ItemStackIngredient input, InfusionStack output) {
        super(id, input, output);
    }

    @Nonnull
    @Override
    public IRecipeType<ItemStackToInfuseTypeRecipe> getType() {
        return MekanismRecipeType.INFUSION_CONVERSION;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<ItemStackToInfuseTypeRecipe> getSerializer() {
        return MekanismRecipeSerializers.INFUSION_CONVERSION.getRecipeSerializer();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return "infusion_conversion";
    }

    @Nonnull
    @Override
    public ItemStack getIcon() {
        return MekanismBlocks.METALLURGIC_INFUSER.getItemStack();
    }
}