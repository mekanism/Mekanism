package mekanism.api.chemical;

import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

//TODO: Rename to tank action or something?
public enum ChemicalAction {
    EXECUTE(FluidAction.EXECUTE),
    SIMULATE(FluidAction.SIMULATE);

    private final FluidAction fluidAction;

    ChemicalAction(FluidAction fluidAction) {
        this.fluidAction = fluidAction;
    }

    public boolean execute() {
        return this == EXECUTE;
    }

    public boolean simulate() {
        return this == SIMULATE;
    }

    public FluidAction toFluidAction() {
        return fluidAction;
    }

    public ChemicalAction combine(boolean execute) {
        return get(execute && execute());
    }

    public static ChemicalAction get(boolean execute) {
        return execute ? EXECUTE : SIMULATE;
    }

    public static ChemicalAction fromFluidAction(FluidAction action) {
        if (action == FluidAction.EXECUTE) {
            return EXECUTE;
        } //else FluidAction.SIMULATE
        return SIMULATE;
    }
}