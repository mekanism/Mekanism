package mekanism.fabric_shim.fluids.capability;

import mekanism.fabric_shim.fluids.FluidStack;

/**
 * Simulate-based fluid handler contract (stand-in for NeoForge's IFluidHandler; same surface).
 * Mekanism's internals keep these semantics; adapters bridge to Fabric's transaction-based
 * Storage&lt;FluidVariant&gt; at the mod boundary (Phase 2).
 */
public interface IFluidHandler {

    enum FluidAction {
        EXECUTE,
        SIMULATE;

        public boolean execute() {
            return this == EXECUTE;
        }

        public boolean simulate() {
            return this == SIMULATE;
        }
    }

    int getTanks();

    FluidStack getFluidInTank(int tank);

    int getTankCapacity(int tank);

    boolean isFluidValid(int tank, FluidStack stack);

    /**
     * @return amount of resource that was (or would have been, if simulated) filled
     */
    int fill(FluidStack resource, FluidAction action);

    /**
     * @return stack representing the fluid that was (or would have been, if simulated) drained
     */
    FluidStack drain(FluidStack resource, FluidAction action);

    /**
     * @return stack representing the fluid that was (or would have been, if simulated) drained
     */
    FluidStack drain(int maxDrain, FluidAction action);
}
