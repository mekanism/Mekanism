package mekanism.common.recipe.machines;

import javax.annotation.Nonnull;
import mekanism.common.recipe.inputs.FluidInput;
import mekanism.common.recipe.outputs.FluidOutput;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class ThermalEvaporationRecipe extends MachineRecipe<FluidInput, FluidOutput, ThermalEvaporationRecipe> {

    public ThermalEvaporationRecipe(@Nonnull FluidStack input, @Nonnull FluidStack output) {
        super(new FluidInput(input), new FluidOutput(output));
    }

    public ThermalEvaporationRecipe(FluidInput input, FluidOutput output) {
        super(input, output);
    }

    @Override
    public ThermalEvaporationRecipe copy() {
        return new ThermalEvaporationRecipe(getInput(), getOutput());
    }

    public boolean canOperate(FluidTank inputTank, FluidTank outputTank) {
        return getInput().useFluid(inputTank, FluidAction.SIMULATE, 1) && getOutput().applyOutputs(outputTank, FluidAction.SIMULATE);
    }

    public void operate(FluidTank inputTank, FluidTank outputTank) {
        if (getInput().useFluid(inputTank, FluidAction.EXECUTE, 1)) {
            getOutput().applyOutputs(outputTank, FluidAction.EXECUTE);
        }
    }
}