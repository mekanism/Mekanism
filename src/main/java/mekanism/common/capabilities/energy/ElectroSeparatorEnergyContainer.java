package mekanism.common.capabilities.energy;

import mekanism.api.IContentsListener;
import mekanism.api.functions.IntObjectToIntFunction;
import mekanism.api.upgrade.Upgrade;
import mekanism.common.tile.machine.TileEntityElectrolyticSeparator;
import net.minecraft.core.Holder;
import org.jspecify.annotations.Nullable;

public class ElectroSeparatorEnergyContainer extends MachineEnergyContainer<TileEntityElectrolyticSeparator> {

    public ElectroSeparatorEnergyContainer(TileEntityElectrolyticSeparator tile, IntObjectToIntFunction<TileEntityElectrolyticSeparator> baseEnergyCalculator,
          @Nullable IContentsListener listener) {
        super(tile, baseEnergyCalculator, listener);
    }

    @Override
    public long calculateBufferMultiplier() {
        long bufferMultiplier = super.calculateBufferMultiplier();
        if (tile.isMakingHydrogen()) {
            bufferMultiplier *= tile.getBaselineMaxOperations();
        }
        return bufferMultiplier;
    }

    @Override
    protected int calculateEnergyPerTick(int baseEnergyPerTick, Holder.@Nullable Reference<Upgrade> speedUpgrade, Holder.@Nullable Reference<Upgrade> energyUpgrade) {
        if (tile.isMakingHydrogen()) {
            //When making hydrogen upgrades do not affect the energy per tick
            return baseEnergyPerTick;
        }
        return super.calculateEnergyPerTick(baseEnergyPerTick, speedUpgrade, energyUpgrade);
    }
}
