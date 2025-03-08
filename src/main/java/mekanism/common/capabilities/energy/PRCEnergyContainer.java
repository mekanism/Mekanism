package mekanism.common.capabilities.energy;

import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.tile.machine.TileEntityPressurizedReactionChamber;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class PRCEnergyContainer extends MachineEnergyContainer<TileEntityPressurizedReactionChamber> {

    public static PRCEnergyContainer input(TileEntityPressurizedReactionChamber tile, @Nullable IContentsListener listener) {
        AttributeEnergy electricBlock = validateBlock(tile);
        return new PRCEnergyContainer(electricBlock.getStorage(), electricBlock.getUsage(), notExternal, ConstantPredicates.alwaysTrue(), tile, listener);
    }

    private PRCEnergyContainer(long maxEnergy, long energyPerTick, Predicate<@NotNull AutomationType> canExtract,
          Predicate<@NotNull AutomationType> canInsert, TileEntityPressurizedReactionChamber tile, @Nullable IContentsListener listener) {
        super(maxEnergy, energyPerTick, canExtract, canInsert, tile, listener);
    }

    @Override
    public long getBaseEnergyPerTick() {
        return super.getBaseEnergyPerTick() + tile.getRecipeEnergyRequired();
    }
}