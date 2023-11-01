package mekanism.api;

import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

public enum Action {
    EXECUTE(FluidAction.EXECUTE),
    SIMULATE(FluidAction.SIMULATE);

    private final FluidAction fluidAction;

    Action(FluidAction fluidAction) {
        this.fluidAction = fluidAction;
    }

    /**
     * @return {@code true} if this action represents execution.
     */
    public boolean execute() {
        return this == EXECUTE;
    }

    /**
     * @return {@code true} if this action represents simulation.
     */
    public boolean simulate() {
        return this == SIMULATE;
    }

    /**
     * Converts this action to the corresponding FluidAction.
     */
    public FluidAction toFluidAction() {
        return fluidAction;
    }

    /**
     * Helper to combines this action with a boolean based execution. This allows easily compounding actions.
     *
     * @param execute {@code true} if it should execute if this action already is an execute action.
     *
     * @return Compounded action.
     */
    public Action combine(boolean execute) {
        return get(execute && execute());
    }

    /**
     * Helper to get an action based on a boolean representing execution.
     *
     * @param execute {@code true} for {@link #EXECUTE}.
     *
     * @return Action.
     */
    public static Action get(boolean execute) {
        return execute ? EXECUTE : SIMULATE;
    }

    /**
     * Helper ot get an action from the corresponding FluidAction.
     *
     * @param action FluidAction.
     *
     * @return Action.
     */
    public static Action fromFluidAction(FluidAction action) {
        if (action == FluidAction.EXECUTE) {
            return EXECUTE;
        } //else FluidAction.SIMULATE
        return SIMULATE;
    }
}