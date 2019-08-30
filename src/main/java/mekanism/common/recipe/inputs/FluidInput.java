package mekanism.common.recipe.inputs;

import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class FluidInput extends MachineInput<FluidInput> {

    @Nonnull
    public FluidStack ingredient = FluidStack.EMPTY;

    public FluidInput(@Nonnull FluidStack stack) {
        ingredient = stack;
    }

    public FluidInput() {
    }

    @Override
    public void load(CompoundNBT nbtTags) {
        ingredient = FluidStack.loadFluidStackFromNBT(nbtTags.getCompound("input"));
    }

    @Override
    public FluidInput copy() {
        return new FluidInput(ingredient.copy());
    }

    @Override
    public boolean isValid() {
        return !ingredient.isEmpty();
    }

    public boolean useFluid(FluidTank fluidTank, FluidAction fluidAction, int scale) {
        if (!fluidTank.getFluid().isEmpty() && fluidTank.getFluid().containsFluid(ingredient)) {
            fluidTank.drain(ingredient.getAmount() * scale, fluidAction);
            return true;
        }
        return false;
    }

    @Override
    public int hashIngredients() {
        return ingredient.getFluid().hashCode();
    }

    @Override
    public boolean testEquality(FluidInput other) {
        if (!isValid()) {
            return !other.isValid();
        }
        return ingredient.equals(other.ingredient);
    }

    @Override
    public boolean isInstance(Object other) {
        return other instanceof FluidInput;
    }
}