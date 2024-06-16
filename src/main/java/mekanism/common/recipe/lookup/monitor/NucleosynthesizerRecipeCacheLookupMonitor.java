package mekanism.common.recipe.lookup.monitor;

import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.Unsigned;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.recipe.lookup.IRecipeLookupHandler;

public class NucleosynthesizerRecipeCacheLookupMonitor extends RecipeCacheLookupMonitor<NucleosynthesizingRecipe> {

    public NucleosynthesizerRecipeCacheLookupMonitor(IRecipeLookupHandler<NucleosynthesizingRecipe> handler) {
        super(handler);
    }

    @Override
    public @Unsigned long updateAndProcess(IEnergyContainer energyContainer) {
        if (!(energyContainer instanceof MachineEnergyContainer<?> machineEnergyContainer)) {
            //Unknown energy container type just don't handle it
            return FloatingLong.ZERO;
        }
        FloatingLong prev = energyContainer.getEnergy().copy();
        if (updateAndProcess()) {
            //TODO: Re-evaluate this at some point
            int toProcess = (int) Math.sqrt(prev.divide(machineEnergyContainer.getEnergyPerTick()).doubleValue());
            for (int i = 0; i < toProcess - 1; i++) {
                cachedRecipe.process();
            }
            //Update amount of energy that actually got used, as if we are "near" full we may not have performed our max number of operations
            return prev.minusEqual(energyContainer.getEnergy());
        }
        //If we don't have a cached recipe so didn't process anything at all just return zero
        return FloatingLong.ZERO;
    }
}