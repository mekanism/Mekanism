package mekanism.common.recipe.outputs;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import net.minecraft.nbt.CompoundNBT;

public class GasOutput extends MachineOutput<GasOutput> {

    public GasStack output;

    public GasOutput(GasStack stack) {
        output = stack;
    }

    public GasOutput() {
    }

    @Override
    public void load(CompoundNBT nbtTags) {
        output = GasStack.readFromNBT(nbtTags.getCompound("output"));
    }

    @Override
    public GasOutput copy() {
        return new GasOutput(output.copy());
    }

    public boolean applyOutputs(GasTank gasTank, boolean doEmit, int scale) {
        if (gasTank.canReceive(output.getGas()) && gasTank.getNeeded() >= output.amount * scale) {
            gasTank.receive(output.copy().withAmount(output.amount * scale), doEmit);
            return true;
        }
        return false;
    }
}