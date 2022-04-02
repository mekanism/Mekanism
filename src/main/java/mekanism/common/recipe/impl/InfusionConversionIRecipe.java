package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class InfusionConversionIRecipe extends ItemStackToInfuseTypeRecipe {

    public InfusionConversionIRecipe(ResourceLocation id, ItemStackIngredient input, InfusionStack output) {
        super(id, input, output);
    }

    @Nonnull
    @Override
    public RecipeType<ItemStackToInfuseTypeRecipe> getType() {
        return MekanismRecipeType.INFUSION_CONVERSION.get();
    }

    @Nonnull
    @Override
    public RecipeSerializer<ItemStackToInfuseTypeRecipe> getSerializer() {
        return MekanismRecipeSerializers.INFUSION_CONVERSION.get();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return "infusion_conversion";
    }

    @Nonnull
    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.METALLURGIC_INFUSER.getItemStack();
    }
}