package mekanism.common.recipe.outputs;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class FluidOutput extends MachineOutput<FluidOutput> {

    public FluidStack output;

    public FluidOutput(FluidStack stack) {
        output = stack;
    }

    public FluidOutput() {
    }

    @Override
    public void load(CompoundNBT nbtTags) {
        output = FluidStack.loadFluidStackFromNBT(nbtTags.getCompound("output"));
    }

    @Override
    public FluidOutput copy() {
        return new FluidOutput(output.copy());
    }

    public boolean applyOutputs(FluidTank fluidTank, FluidAction fluidAction) {
        if (fluidTank.fill(output, FluidAction.SIMULATE) > 0) {
            fluidTank.fill(output, fluidAction);
            return true;
        }
        return false;
    }
}