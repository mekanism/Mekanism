package mekanism.common.recipe.lookup.monitor;

import com.google.common.primitives.Ints;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.recipe.lookup.IRecipeLookupHandler;
import net.minecraft.world.level.Level;

public class NucleosynthesizerRecipeCacheLookupMonitor extends ProgressRecipeCacheLookupMonitor<NucleosynthesizingRecipe> {

    public NucleosynthesizerRecipeCacheLookupMonitor(IRecipeLookupHandler<NucleosynthesizingRecipe> handler) {
        super(handler, true);
    }

    public int updateAndProcess(Level level, MachineEnergyContainer<?> energyContainer) {
        long prev = energyContainer.getAmountAsLong();
        if (updateAndProcess(level) && cachedRecipe != null) {
            //TODO: Re-evaluate this at some point
            int toProcess = (int) Math.sqrt(prev / (double) energyContainer.getEnergyPerTick());
            for (int i = 0; i < toProcess - 1; i++) {
                cachedRecipe.process();
            }
            //Update amount of energy that actually got used, as if we are "near" full we may not have performed our max number of operations
            return Ints.saturatedCast(prev - energyContainer.getAmountAsLong());
        }
        //If we don't have a cached recipe so didn't process anything at all just return zero
        return 0;
    }
}