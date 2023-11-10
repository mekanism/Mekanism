package mekanism.common.recipe.impl;

import java.util.Optional;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

@NothingNullByDefault
public class SawmillIRecipe extends SawmillRecipe {

    public SawmillIRecipe(ItemStackIngredient input, ItemStack mainOutput, ItemStack secondaryOutput, double secondaryChance) {
        super(input, mainOutput, secondaryOutput, secondaryChance);
    }

    @Override
    public RecipeType<SawmillRecipe> getType() {
        return MekanismRecipeType.SAWING.get();
    }

    @Override
    public RecipeSerializer<SawmillIRecipe> getSerializer() {
        return MekanismRecipeSerializers.SAWING.get();
    }

    @Override
    public String getGroup() {
        return MekanismBlocks.PRECISION_SAWMILL.getName();
    }

    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.PRECISION_SAWMILL.getItemStack();
    }

    public Optional<ItemStack> getMainOutputRaw() {
        return this.mainOutput.isEmpty() ? Optional.empty() : Optional.of(this.mainOutput);
    }

    public Optional<ItemStack> getSecondaryOutputRaw() {
        return this.secondaryOutput.isEmpty() ? Optional.empty() : Optional.of(this.secondaryOutput);
    }
}