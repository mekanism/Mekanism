package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.common.MekanismBlock;
import mekanism.common.recipe.MekanismRecipeSerializers;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

public class FluidToFluidIRecipe extends FluidToFluidRecipe implements IRecipe<IInventory> {

    private final ResourceLocation id;

    public FluidToFluidIRecipe(ResourceLocation id, FluidStackIngredient input, FluidStack output) {
        super(input, output);
        this.id = id;
    }

    @Override
    public boolean matches(@Nonnull IInventory inv, @Nonnull World world) {
        //TODO: Check if this matches properly, maybe require some IInventory that gives more information about slots
        return true;
    }

    @Override
    public boolean isDynamic() {
        //TODO: If we make this non dynamic, we can make it show in vanilla's crafting book
        // and also then obey the recipe locking.
        // For now none of that works/makes sense in our concept so don't lock it
        return true;
    }

    @Nonnull
    @Override
    public ItemStack getCraftingResult(@Nonnull IInventory inv) {
        //TODO
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canFit(int width, int height) {
        //TODO: This is always true for cooking recipes, so I assume we probably also want it true
        return true;
    }

    @Nonnull
    @Override
    public ItemStack getRecipeOutput() {
        //TODO
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Nonnull
    @Override
    public IRecipeType<FluidToFluidIRecipe> getType() {
        return MekanismRecipeType.THERMAL_EVAPORATION_PLANT;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<FluidToFluidIRecipe> getSerializer() {
        return MekanismRecipeSerializers.THERMAL_EVAPORATION_PLANT;
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlock.THERMAL_EVAPORATION_CONTROLLER.getName();
    }

    @Nonnull
    @Override
    public ItemStack getIcon() {
        return MekanismBlock.THERMAL_EVAPORATION_CONTROLLER.getItemStack();
    }
}